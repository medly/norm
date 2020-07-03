package norm

import java.sql.Connection
import java.sql.ResultSet

/*
 * Makes working with JDBC/Postgres less terrible
 * with some inspiration from now deprecated: https://github.com/Kotlin/obsolete-kotlin-jdbc
 */


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

fun Connection.query(sql: String, vararg params: Any) =
        this.executeQuery(sql, params.toList()).toList()
