package com.medly.norm

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.testfixtures.ProjectBuilder

class NormPluginTest : StringSpec() {  // TODO: Remove duplicate tests from integration tests
    init {

        "project should have compileNorm task" {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("org.jetbrains.kotlin.jvm")
            project.pluginManager.apply(NormPlugin::class.java)

            shouldNotThrow<UnknownTaskException> {
                project.tasks.getAt("compileNorm")
            }
        }

        "project should have norm configuration" {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("org.jetbrains.kotlin.jvm")
            project.pluginManager.apply(NormPlugin::class.java)

            shouldNotThrow<UnknownConfigurationException> {
                project.configurations.getAt("norm")
            }
        }

        "project should have norm runtime implementation dependencies" {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply("org.jetbrains.kotlin.jvm")
            project.pluginManager.apply(NormPlugin::class.java)

            shouldNotThrow<UnknownConfigurationException> {
                project.configurations.getAt("implementation").dependencies.map {
                    "${it.group}:${it.name}:${it.version}"
                } shouldContainAll listOf(
                    "com.medly.norm:runtime:0.0.4"
                )
            }
        }
    }
}
