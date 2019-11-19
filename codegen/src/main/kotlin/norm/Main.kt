package norm

import org.postgresql.ds.PGSimpleDataSource
import java.io.File
import java.sql.Connection

fun main(args: Array<String>) {
    val sourceDir = File(args[0])
    val outputDir = File(args[1])

    val dataSource = PGSimpleDataSource().also {
        it.setUrl(args.getOrElse(2) { _ ->"jdbc:postgresql://localhost/postgres"})
        it.user = args.getOrElse(3) { _ -> "postgres" }
        it.password = args.getOrNull(4)
    }

    println(sourceDir)
    println(outputDir)

    if (!sourceDir.isDirectory || !outputDir.isDirectory) {
        println("invalid source or output directory")
    }

    globSearch(sourceDir, "**.sql")
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
