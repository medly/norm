package norm.analyzer

import norm.model.ColumnModel
import norm.model.ParamModel
import norm.model.SqlModel
import norm.util.toCamelCase
import java.sql.Connection
import java.sql.ParameterMetaData
import java.sql.ResultSetMetaData

/**
 * Can Analyze the Tables and PreparedStatements without executing Queries.
 */
class SqlAnalyzer(private val connection: Connection) {
    private val namedParamsRegex = "(?<!:)(:\\w+)".toRegex() // TODO extract
    private val leftJoinRegex = "(?i)(?:LEFT\\s+JOIN\\s+)(?<tables>\\b\\S+\\b)".toRegex()

    fun sqlModel(namedParamSql: String): SqlModel {

        val paramNames = namedParamsRegex.findAll(namedParamSql).map { it.value }.toList()
        val preparableStatement = namedParamsRegex.replace(namedParamSql, "?")
        val preparedStatement = connection.prepareStatement(preparableStatement)

        val parameterMetaData = preparedStatement.parameterMetaData
        val params = (1..parameterMetaData.parameterCount).map {
            ParamModel(
                    paramNames[it - 1].substring(1),
                    parameterMetaData.getParameterTypeName(it), // db type
                    parameterMetaData.isNullable(it) != ParameterMetaData.parameterNoNulls
            )
        }

        val leftJoinedTables = leftJoinRegex.findAll(namedParamSql).map { it.groups[1]?.value }.toList()
        val resultSetMetaData: ResultSetMetaData? = preparedStatement.metaData
        val columns = if (resultSetMetaData != null) { // it is a query

            (1..resultSetMetaData.columnCount).map {
                val isNullable = leftJoinedTables.contains(resultSetMetaData.getTableName(it)) ||
                    (resultSetMetaData.isNullable(it) != ResultSetMetaData.columnNoNulls)
                ColumnModel(
                        toCamelCase(resultSetMetaData.getColumnName(it)),
                        resultSetMetaData.getColumnTypeName(it),
                        resultSetMetaData.getColumnName(it),
                        isNullable
                )
            }
        } else { // it is a command
            listOf<ColumnModel>()
        }
        return SqlModel(params, columns, preparableStatement)
    }
}
