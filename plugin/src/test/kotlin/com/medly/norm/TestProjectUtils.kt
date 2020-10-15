package com.medly.norm

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun File.defaultProjectSetup(configuration: String = "") {
    val projectDir = "\$projectDir"
    //language=Groovy
    resolve("build.gradle").writeText(
        """
            plugins {
                id 'org.jetbrains.kotlin.jvm' version '1.3.72'
                id 'com.medly.norm'
            }

            repositories {
                gradlePluginPortal()
                maven {
                    url 'https://jitpack.io'
                }
            }

            norm {
                inputDir = file('src/main/kotlin/sql')
                outDir = file('src/main/kotlin/gen')
            }

        """.trimIndent()
    )
}

fun File.createSourceFile(sourceFilePath: String, contents: String) {
    val sourceFile = resolve(sourceFilePath)
    sourceFile.parentFile.mkdirs()
    sourceFile.writeText(contents)
}

fun File.build(
    vararg arguments: String
): BuildResult = GradleRunner.create()
    .withProjectDir(this)
    .withPluginClasspath()
    .withArguments(arguments.toList() + "--stacktrace")
    .forwardOutput().build()
