package norm.api

import norm.analyzer.SqlAnalyzer
import norm.codegen.CodeGenerator
import java.sql.Connection

/**
 * Initialize with a given postgres connection
 *
 * Generates code of classes against given SQL Query
 */
class NormApi(
        connection: Connection
) {
    private val sqlAnalyzer = SqlAnalyzer(connection)
    /**
     * Generates code of classes against given SQL Query
     */
    fun generate(query: String, packageName: String, baseName: String): String {
        val sqlModel = sqlAnalyzer.sqlModel(query)
        return CodeGenerator.generate(listOf(sqlModel), packageName, baseName)
    }
}
