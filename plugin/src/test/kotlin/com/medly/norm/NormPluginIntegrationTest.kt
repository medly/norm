package com.medly.norm

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.sequences.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File

class NormPluginIntegrationTest : StringSpec() {

    private val pgContainer = PGContainer().withInitScript("norm-plugin-init.sql")
    private val tmpDir = createTempDir()
    private val project = tmpDir.resolve("plugin-test").apply { mkdirs() }

    override fun listeners() = listOf(pgContainer.perSpec())

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        File(tmpDir.toURI()).deleteRecursively()
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        //language=Groovy
        project.defaultProjectSetup("""
            norm {
                username = "${pgContainer.username}"
                password = "${pgContainer.password}"
                jdbcUrl = "${pgContainer.jdbcUrl}"
                inputDir = file("src/main/kotlin/sql")
                outDir = file("src/main/kotlin/gen")
            }
        """.trimIndent())
    }

    init {

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
                        dependency.contains("com.medly.norm:codegen")
                    }
                normDependencies shouldHaveSize 1
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

        "project should generate files on calling compileNorm task" {
            project.createSourceFile(
                "src/main/kotlin/sql/users/find-users.sql",
                """
                select * from users where name = :name;
                """.trimIndent()
            )
            project.build("compileNorm")
            File("${project.absolutePath}/src/main/kotlin/gen/users/FindUsers.kt").exists() shouldBe true
        }
    }
}
