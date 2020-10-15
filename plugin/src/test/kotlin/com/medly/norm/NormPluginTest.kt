package com.medly.norm

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.sequences.shouldHaveSize
import java.io.File

class NormPluginTest : StringSpec() {

    private val tmpDir = createTempDir()
    private val project = tmpDir.resolve("plugin-test").apply { mkdirs() }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        File(tmpDir.toURI()).deleteRecursively()
    }

    init {
        //language=Groovy
        project.defaultProjectSetup("""
            norm {
                inputDir = dir("src/main/kotlin/sql")
                outDir = dir("src/main/kotlin/gen")
            }
        """.trimIndent())

        "project should have compileNorm task" {

            project.build("tasks", "--all").apply {
                val compileNormTasks = output.lineSequence()
                    .filter { task -> task.contains("compileNorm") }
                compileNormTasks shouldHaveSize 1
            }
        }

        "project should have norm dependencies" {
            project.build("dependencies", "--configuration", "norm").apply {
                val normDependencies = output.lineSequence()
                    .filter { dependency ->
                        dependency.contains("com.medly.norm:codegen").or(
                            dependency.contains("com.medly.norm:runtime")
                        )
                    }
                normDependencies shouldHaveSize 2
            }
        }

        "project should have norm runtime implementation dependency" {
            project.build("dependencies", "--configuration", "implementation").apply {
                val dependencies = output.lineSequence()
                    .filter { dependency ->
                        dependency.contains("com.medly.norm:runtime")
                    }
                dependencies shouldHaveSize 1
            }
        }

        //FIXME: Work in progress
        // "project should generate files on calling compileNorm task" {
        //     project.createSourceFile(
        //         "src/main/kotlin/sql/find-users.sql", """
        //         select * from users;
        //     """.trimIndent()
        //     )
        //     project.build("compileNorm")
        //     project.listFiles { _, name -> name == "FindUsers.kt" }!! shouldHaveSize 1
        // }
    }
}
