package com.figure.gradle.semver.v1

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class SemverExtension @Inject constructor(objects: ObjectFactory, project: Project) {
    val gitDir: Property<String> = objects.property<String>().convention("${project.rootProject.rootDir.path}/.git")
    val tagPrefix: Property<String> = objects.property<String>().convention("TODO")
    val initialVersion: Property<String> = objects.property<String>().convention("TODO")
    val versionStrategy: ListProperty<String> = objects.listProperty<String>().convention(null)

    val version by lazy { "TODO" }
    val versionTagName by lazy { "TODO" }
}
