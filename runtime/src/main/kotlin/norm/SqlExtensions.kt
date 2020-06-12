package norm

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * Makes working with JDBC/Postgres less terrible
 *
 *
 * with some inspiration from now deprecated: https://github.com/Kotlin/obsolete-kotlin-jdbc
 */


operator fun ResultSet.get(columnId: Int): Any? = this.getObject(columnId)

operator fun ResultSet.get(columnName: String): Any? = this.getObject(columnName) // TODO specific type methods instead of getObject

fun ResultSet.getColumnNames(): List<String> =
    (1..metaData.columnCount).map { metaData.getColumnName(it) }

fun ResultSet.rowAsList(columnNames: List<String> = getColumnNames()): List<Any?> = // TODO Can overload for column indexes instead of name
    columnNames.map { this[it] }

fun ResultSet.rowAsMap(columnNames: List<String> = getColumnNames()): Map<String, Any?> =
    columnNames.map { it to this[it] }.toMap()

fun ResultSet.toList(columnNames: List<String> = getColumnNames()): List<Map<String, Any?>> =
    this.use { generateSequence { if (this.next()) this.rowAsMap(columnNames) else null }.toList() }

fun ResultSet.toTable(columnNames: List<String> = getColumnNames()): List<List<Any?>> =
    this.use { generateSequence { if (this.next()) this.rowAsList(columnNames) else null }.toList() }


// TODO - handle prepareStatements Failure as well

fun PreparedStatement.withParams(params: List<Any?> = listOf()): PreparedStatement =
    this.also { self ->
        params.forEachIndexed { index, param -> self.setObject(index + 1, param) }
    }

fun PreparedStatement.withBatches(batchedParams: List<List<Any?>> = listOf()) =
    this.also { ps ->
        batchedParams.forEach { params ->
            ps.withParams(params).addBatch()
        }
    }

fun Connection.executeCommand(sql: String, params: List<Any> = listOf()): Int =
    this.prepareStatement(sql)
        .withParams(params)
        .use { it.executeUpdate() } // auto-close ps

fun Connection.batchExecuteCommand(sql: String, batchedParams: List<List<Any?>> = listOf()): List<Int> =
    this.prepareStatement(sql)
        .withBatches(batchedParams)
        .use { it.executeBatch() }
        .toList()

fun Connection.executeQuery(sql: String, params: List<Any> = listOf()): ResultSet =
    this.prepareStatement(sql)
        .withParams(params)
        .executeQuery()

fun <T> DataSource.map(sql: String, vararg params: Any, mapper: (Map<String, Any?>) -> T): List<T> =
    this.query(sql, *params).map(mapper)

fun Connection.query(sql: String, vararg params: Any) =
    this.executeQuery(sql, params.toList()).toList()

fun DataSource.query(sql: String, vararg params: Any) =
    this.connection.use { it.executeQuery(sql, params.toList()).toList() }

fun DataSource.command(sql: String, vararg params: Any) =
    this.connection.use { it.executeCommand(sql, params.toList()) }
