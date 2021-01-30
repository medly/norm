package norm.test.utils

import norm.analyzer.SqlAnalyzer
import norm.codegen.CodeGenerator
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection

class PgContainer : PostgreSQLContainer<PgContainer>()

fun toArgs(str: String): Array<String> = str.split(" ").toTypedArray()

fun codegen(conn: Connection, query: String, pkg: String, base: String): String {
    val sqlModel = SqlAnalyzer(conn).sqlModel(query)
    return CodeGenerator().generate(sqlModel, pkg, base).trim()
}

fun String.readAsResource(): String = object {}.javaClass.getResource(this).readText().trim()
