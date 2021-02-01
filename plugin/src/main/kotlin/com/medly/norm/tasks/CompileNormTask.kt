package com.medly.norm.tasks

import norm.cli.main
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

@CacheableTask
open class CompileNormTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:Input
    val userName: Property<String> = objects.property(String::class.java)

    @get:Input
    val password: Property<String> = objects.property(String::class.java)

    @get:Input
    val jdbcUrl: Property<String> = objects.property(String::class.java)

    @get:InputFiles
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val inputFilesAsOpts: ConfigurableFileCollection = objects.fileCollection()

    @get:InputFiles
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sqlFiles: ConfigurableFileCollection = objects.fileCollection()

    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val basePath: DirectoryProperty = objects.directoryProperty()

    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val input: DirectoryProperty = objects.directoryProperty()

    @get:OutputDirectory
    val output: DirectoryProperty = objects.directoryProperty()

    init {
        description = "generate norm classes for your SQLs"
    }

    @TaskAction
    fun compileNorm() {
        main(getArguments())
    }

    private fun getArguments(): Array<String> {
        val namedArguments: List<Pair<String, String?>> = listOf(
            "-u" to userName.get(),
            "-p" to password.get(),
            "-j" to jdbcUrl.get(),
            "-o" to output.get().asFile.absolutePath,
            "-b" to basePath.takeIf { it.isPresent }?.asFile?.get()?.absolutePath,
            "-d" to input.takeIf { it.isPresent }?.asFile?.get()?.absolutePath
        ) + inputFilesAsOpts.files.map { "-f" to it.absolutePath }

        return namedArguments
            .filter { (_, argValue) -> argValue != null }
            .flatMap { (argName, argValue) -> listOf(argName, argValue!!) }
            .toTypedArray()
    }
}
