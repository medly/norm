package com.medly.norm

import com.medly.norm.Constants.CONFIGURATION_IMPLEMENTATION
import com.medly.norm.Constants.EXTENSION_NORM
import com.medly.norm.Constants.NORM_CODEGEN_TASK
import com.medly.norm.Constants.NORM_RUNTIME_DEPENDENCY
import com.medly.norm.extensions.NormExtension
import com.medly.norm.tasks.NormCodegenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

class NormPlugin : Plugin<Project> {

    private val projectVersion = VersionLoader.getVersion()

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NORM, NormExtension::class.java, project.objects
        )
        addImplementationDependencies(project)
        registerNormTasks(project, extension)
    }

    private fun registerNormTasks(project: Project, extension: NormExtension) {
        project.tasks.register(NORM_CODEGEN_TASK, NormCodegenTask::class.java) {
            it.sqlFiles.setFrom(extension.sqlFiles)
            it.inputFilesAsOpts.setFrom(extension.inputFilesAsOpts)
            it.basePath.set(extension.basePath)
            it.input.set(extension.inputDir)
            it.output.set(extension.outDir)
            it.userName.set(extension.username)
            it.password.set(extension.password)
            it.jdbcUrl.set(extension.jdbcUrl)
        }
    }

    private fun addImplementationDependencies(project: Project) {
        project.configurations.getByName(CONFIGURATION_IMPLEMENTATION) { configuration ->
            configuration.dependencies.add(getNormImplementationDependency(project))
        }
    }

    private fun getNormImplementationDependency(project: Project): Dependency =
        project.dependencies.create("$NORM_RUNTIME_DEPENDENCY:${projectVersion}")
}
