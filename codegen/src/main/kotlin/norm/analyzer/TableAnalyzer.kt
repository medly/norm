package norm.analyzer

import norm.model.ColumnModel
import norm.model.TableModel
import norm.util.toCamelCase
import norm.toList
import java.sql.Connection

class TableAnalyzer(private val connection: Connection) {
    fun tableModel(catalog: String? = null, schema: String? = null, table: String): TableModel {
        val databaseMetaData = connection.metaData
        val cols = databaseMetaData
            .getColumns(catalog, schema, table.toLowerCase(), null)
            .toList().map { fromHashMap(it) }
        val pks = databaseMetaData
            .getPrimaryKeys(catalog, schema, table.toLowerCase())
            .toList().map { fromHashMap(it) }

        return TableModel(cols, pks)
    }

    private fun fromHashMap(it: Map<String, Any?>): ColumnModel {
        return ColumnModel(
                fieldName = toCamelCase(it["COLUMN_NAME"].toString()),
                colType = it["TYPE_NAME"].toString(),
                isNullable = true,
                colName = it["COLUMN_NAME"].toString()
        )
    }
}
