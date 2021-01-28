package com.medly.norm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
open class CompileNormTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:Classpath
    val normClasspath: ConfigurableFileCollection = objects.fileCollection()

    @get:Input
    val userName: Property<String> = objects.property(String::class.java)

    @get:Input
    val password: Property<String> = objects.property(String::class.java)

    @get:Input
    val jdbcUrl: Property<String> = objects.property(String::class.java)

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
        val execResult = project.javaexec {
            it.classpath = normClasspath
            it.main = "norm.MainKt"
            it.args = listOf(
                input.asFile.get().absolutePath,
                output.asFile.get().absolutePath,
                jdbcUrl.get(),
                userName.get(),
                password.get()
            )
        }
        println(execResult.exitValue)
    }
}
