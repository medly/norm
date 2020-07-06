package norm.fs

import norm.util.toTitleCase
import java.io.File

class IO(
        private val sqlFile: File,
        baseDir: File,
        outDir: File
) {
    private val sqlFileRelativeToSource = sqlFile.relativeTo(baseDir)
    private val nameWithoutExtension = sqlFileRelativeToSource.nameWithoutExtension
    private val parentPath = sqlFileRelativeToSource.parent
    private val packageName = parentPath.replace(File.separator, ".")
    private val baseName = toTitleCase(nameWithoutExtension)
    private val outFileParentDir = File(outDir, parentPath)
    private val outputFile = File(outFileParentDir, "$baseName.kt")

    fun process(block: (query: String, packageName: String, baseName: String) -> String) {
        outFileParentDir.mkdirs()
        println("will write to $outputFile")
        outputFile.writeText(block(sqlFile.readText(), packageName, baseName))
    }
}

