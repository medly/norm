package norm

import org.postgresql.ds.PGSimpleDataSource
import java.io.File
import java.sql.Connection
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        try {
            val sourceDir = File(args[0])
            val outputDir = File(args[1])

            val dataSource = PGSimpleDataSource().also {
                it.setUrl(args.getOrElse(2) { "jdbc:postgresql://localhost/postgres" })
                it.user = args.getOrElse(3) { "postgres" }
                it.password = args.getOrNull(4)
            }

            println(sourceDir)
            println(outputDir)

            if (!sourceDir.isDirectory || !outputDir.isDirectory) {
                println("invalid source or output directory")
            }

            getSqlFilesToCompile(sourceDir)
                .forEach { sqlFile ->
                    val sqlFileRelativeToSource = sqlFile.relativeTo(sourceDir)
                    val nameWithoutExtension = sqlFileRelativeToSource.nameWithoutExtension
                    val innerPath = sqlFileRelativeToSource.parent
                    val packageName = innerPath.replace(File.separator, ".")
                    val baseName = toTitleCase(nameWithoutExtension)

                    dataSource.connection.use { connection ->
                        val generatedFileContent = codegen(connection, sqlFile.readText(), packageName, baseName)
                        val outFileParentDir = File(outputDir, innerPath)
                        outFileParentDir.mkdirs()
                        File(outFileParentDir, "$baseName.kt").writeText(generatedFileContent)
                    }
                }
        } catch (e: Exception) {
            println(e.message)
            println(e.stackTrace)
        } finally {
            exitProcess(0)
        }
    }
}

fun codegen(
    connection: Connection,
    query: String,
    packageName: String,
    baseName: String
): String {
    val sqlModel = SqlAnalyzer(connection).sqlModel(query)

    return CodeGenerator().generate(sqlModel, packageName, baseName)
}
