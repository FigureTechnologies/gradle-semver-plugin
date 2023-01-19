package com.figure.gradle.semver.v1.internal

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

internal inline fun <reified T> ObjectFactory.property(
    configuration: Property<T>.() -> Unit = {}
): Property<T> = property(T::class.java).apply(configuration)
