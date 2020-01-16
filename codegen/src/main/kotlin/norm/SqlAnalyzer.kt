package norm

import java.sql.Connection
import java.sql.ParameterMetaData
import java.sql.ResultSetMetaData

data class TableModel(
    val columns: List<ColumnModel>,
    val primaryKeys: List<ColumnModel>
)

data class ColumnModel(
    val fieldName: String,
    val colType: String,
    val colName: String,
    val isNullable: Boolean
)

data class ParamModel(
    val name: String,
    val dbType: String,
    val isNullable: Boolean,
    val places: List<Int>
)

data class SqlModel(
    val params: List<ParamModel>,
    val cols: List<ColumnModel>,
    val preparableStatement: String
)

/**
 * Can Analyze the Tables and PreparedStatements without executing Queries.
 */
class SqlAnalyzer(private val connection: Connection) {
    val namedParamsRegex = "(?<!:)(:\\w+)".toRegex() // TODO extract

    fun sqlModel(namedParamSql: String): SqlModel {

        val paramNames = namedParamsRegex.findAll(namedParamSql).map { it.value }.toList()
        val paramsIndexMapping = paramNames.foldIndexed(HashMap<String, List<Int>>(), { i, acc, param ->
            acc[param] = acc[param]?.let { it + (i + 1) } ?: listOf(i + 1)
            acc
        })

        val preparableStatement = namedParamsRegex.replace(namedParamSql, "?")
        val preparedStatement = connection.prepareStatement(preparableStatement)

        val parameterMetaData = preparedStatement.parameterMetaData

        val params = paramsIndexMapping.map {
            ParamModel(
                it.key.substring(1),
                parameterMetaData.getParameterTypeName(it.value[0]), // db type
                parameterMetaData.isNullable(it.value[0]) != ParameterMetaData.parameterNoNulls,
                it.value
            )
        }

        val resultSetMetaData: ResultSetMetaData? = preparedStatement.metaData

        val res = if (resultSetMetaData != null) { // it is a query
            (1..resultSetMetaData.columnCount).map {
                ColumnModel(
                    toCamelCase(resultSetMetaData.getColumnName(it)),
                    resultSetMetaData.getColumnTypeName(it),
                    resultSetMetaData.getColumnName(it),
                    resultSetMetaData.isNullable(it) != ResultSetMetaData.columnNoNulls
                )
            }
        } else { // it is a command
            listOf()
        }
        return SqlModel(params, res, preparableStatement)
    }
}
