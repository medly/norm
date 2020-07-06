package norm.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import norm.api.NormApi
import norm.fs.IO
import norm.fs.globSearch
import norm.util.withPGConnection
import java.io.File
import kotlin.system.exitProcess


/**
 * Entry point - The main function
 */
fun main(args: Array<String>) = NormCli().main(args)


/**
 * Implementation of CLI using Norm API
 *
 * Can use env variable to pass in sensitive information
 */
class NormCli : CliktCommand( // command name is inferred as norm-cli
        name = "norm-codegen",
        help = """
            Generates Kotlin Source files for given SQL files using the Postgres database connection
        """
) {

    private val jdbcUrl by option("-j", "--jdbc-url", help = "JDBC connection URL (can use env var PG_JDBC_URL)", envvar = "PG_JDBC_URL")
            .default("jdbc:postgresql://localhost/postgres")

    private val username by option("-u", "--username", help = "Username (can use env var PG_USERNAME)", envvar = "PG_USERNAME")
            .default("postgres")

    private val password by option("-p", "--password", help = "Password (can use env var PG_PASSWORD)", envvar = "PG_PASSWORD")
            .default("")

    private val basePath by option("-b", "--base-path", help = " relative path from this dir will be used to infer package name")
            .file(canBeFile = false, canBeDir = true, mustExist = true)
            .default(File(".")) // Current working dir

    private val inputFilesAsOpts by option("-f", "--file", help = "[Multiple] SQL files, the file path relative to base path (-b) will be used to infer package name")
            .file(canBeFile = true, canBeDir = false, mustExist = true)
            .multiple()
            .unique()

    private val sqlFiles by argument() // give meaningful name for CLI help message
            .file(canBeFile = true, canBeDir = false, mustExist = true)
            .multiple()
            .unique()

    private val inputDir by option("-d", "--in-dir", help = "Dir containing .sql files, relative path from this dir will be used to infer package name")
            .file(canBeFile = false, canBeDir = true, mustExist = true)

    private val outDir by option("-o", "--out-dir", help = "Output dir where source should be generated")
            .file(canBeFile = false, canBeDir = true, mustExist = true)
            .required()


    override fun run() {
        try {
            withPGConnection(jdbcUrl, username, password) { connection ->
                val normApi = NormApi(connection)

                // If dir is provided, relativize to itself
                inputDir?.let { dir ->
                    globSearch(dir, "**.sql").forEach { sqlFile ->
                        IO(sqlFile, dir, outDir).process(normApi::generate)
                    }
                }

                // if file list is explicitly provided, it is relative to basePath
                (inputFilesAsOpts + sqlFiles).forEach { sqlFile ->
                    IO(sqlFile, basePath, outDir).process(normApi::generate)
                }
            }
            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
    }
}


