package norm

import org.apache.commons.text.CaseUtils
import java.io.File
import java.nio.file.FileSystems

fun toCamelCase(s: String): String = CaseUtils.toCamelCase(s, false, '_', '-', ' ')

fun toTitleCase(s: String): String = CaseUtils.toCamelCase(s, true, '_', '-')

fun getSqlFilesToCompile(sourceDir: File): Sequence<File> {
    val sqlScripts = System.getProperty("scripts")
    return sqlScripts?.split(",")?.map { File(it) }?.asSequence()
        ?: globSearch(sourceDir, "**.sql")
}

fun globSearch(root: File, pattern: String): Sequence<File> {
    val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")!!
    return root.walkTopDown().filter { file -> pathMatcher.matches(file.toPath()) }
}
