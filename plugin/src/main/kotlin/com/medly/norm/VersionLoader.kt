package com.medly.norm

import java.util.*

object VersionLoader {
    private const val VERSION_PROPERTIES_PATH: String = "/version.properties"

    fun getVersion(): String =
        this.javaClass.getResourceAsStream(VERSION_PROPERTIES_PATH)?.use { versionStream ->
            Properties()
                .also { it.load(versionStream) }
                .let { it.getProperty("project.version") ?: "unspecified" }
        } ?: throw RuntimeException(
            """
            Could not load version.properties.
            This could happen when plugin jar is deleted
            Try Stopping Gradle Daemon (gradle --stop)""".trimIndent()
        )
}
