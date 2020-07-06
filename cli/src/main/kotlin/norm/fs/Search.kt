package norm.fs

import java.io.File
import java.nio.file.FileSystems

/**
 * Searches within a directory using glob style patterns
 *
 * example "**.txt"
 *
 * @param root must be Root Directory in which search will be performed
 * @param pattern must be valid Glob Pattern
 *
 */
fun globSearch(root: File, pattern: String): Sequence<File> {
    val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")!!
    return root.walkTopDown().filter { file -> pathMatcher.matches(file.toPath()) }
}
