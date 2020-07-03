package norm

import java.sql.ResultSet

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
