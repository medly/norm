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
    val namedParamsRegex = "(?<!:)(:\\w+)".toRegex() // TODO extract

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
            listOf<ColumnModel>()
        }
        return SqlModel(params, res, preparableStatement)
    }
}
