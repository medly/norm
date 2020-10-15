package com.medly.norm.extensions

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class NormExtension @Inject constructor(
    objectFactory: ObjectFactory
) {
    val version: Property<String> = objectFactory.property { set("0.0.4") }
    val username: Property<String> = objectFactory.property { set("postgres") }
    val password: Property<String> = objectFactory.property { set("") }
    val basePath: RegularFileProperty = objectFactory.fileProperty()
    val inputFilesAsOpts: ConfigurableFileCollection = objectFactory.fileCollection()
    val sqlFiles: ConfigurableFileCollection = objectFactory.fileCollection()
    val inputDir: DirectoryProperty = objectFactory.directoryProperty()
    val outDir: DirectoryProperty = objectFactory.directoryProperty()
}


internal inline fun <reified T> ObjectFactory.property(
    configuration: Property<T>.() -> Unit = {}
) = property(T::class.java).apply(configuration)
