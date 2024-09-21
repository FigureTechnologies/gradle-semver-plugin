/*
 * Copyright (C) 2024 Figure Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.figure.gradle.semver.internal.properties

import com.figure.gradle.semver.internal.extensions.gradle
import com.figure.gradle.semver.internal.extensions.projectDir
import com.figure.gradle.semver.internal.extensions.providers
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.Provider
import java.io.File
import java.util.Properties

private const val GRADLE_PROPERTIES = "gradle.properties"

val PluginAware.modifier: Provider<Modifier>
    get() = semverProperty(SemverProperty.Modifier).map { Modifier.fromValue(it) }.orElse(Modifier.Auto)

val PluginAware.stage: Provider<Stage>
    get() = semverProperty(SemverProperty.Stage).map { Stage.fromValue(it) }.orElse(Stage.Auto)

val PluginAware.tagPrefix: Provider<String>
    get() = semverProperty(SemverProperty.TagPrefix).orElse("v")

val PluginAware.overrideVersion: Provider<String>
    get() = semverProperty(SemverProperty.OverrideVersion)

val PluginAware.forMajorVersion: Provider<Int>
    get() =
        semverProperty(SemverProperty.ForMajorVersion).map {
            runCatching {
                it.toInt()
            }.getOrElse {
                error("semver.forMajorVersion must be representative of a valid major version line (0, 1, 2, etc.)")
            }
        }

val PluginAware.appendBuildMetadata: Provider<String>
    get() = semverProperty(SemverProperty.AppendBuildMetadata)

val PluginAware.forTesting: Provider<Boolean>
    get() = semverProperty(SemverProperty.ForTesting).map { it.toBoolean() }.orElse(false)

private fun PluginAware.gradlePropertiesProperty(
    semverProperty: SemverProperty,
    propertiesDirectory: File,
): Provider<String> {
    return if (propertiesDirectory.resolve(GRADLE_PROPERTIES).exists()) {
        providers.provider {
            Properties().apply {
                propertiesDirectory.resolve(GRADLE_PROPERTIES).inputStream().use { load(it) }
            }.getProperty(semverProperty.property)
        }
    } else {
        providers.provider { null }
    }
}

/**
 * This will search for the semver property in the following order:
 * 1. Start parameter. ie: `-Psemver.<property>=value` via cli
 * 2. `gradle.properties` in the gradle user home directory
 * 3. `gradle.properties` in the gradle home directory
 * 4. `gradle.properties` in the project directory
 *
 * This search order is different compared to the standard search order implemented by Gradle itself.
 * Gradle typically searches by project directory first, then gradle user home, then gradle home.
 *
 * Why the difference? Since this is primarily a developer preferred property, it makes sense to prefer
 * the gradle user home and gradle home directory, which are not checked into a repository, over the project
 * directory. This way, developers can have their own personal settings that are not checked into the repository.
 */
private fun PluginAware.semverProperty(semverProperty: SemverProperty): Provider<String> =
    when {
        gradle.startParameter.projectProperties[semverProperty.property] != null -> {
            providers.provider { gradle.startParameter.projectProperties[semverProperty.property] }
        }

        gradlePropertiesProperty(semverProperty, gradle.gradleUserHomeDir).isPresent -> {
            gradlePropertiesProperty(semverProperty, gradle.gradleUserHomeDir)
        }

        gradle.gradleHomeDir?.exists() == true -> {
            // C'mon compiler, you can be smarter than this to be able to do the smart cast!
            gradlePropertiesProperty(semverProperty, gradle.gradleHomeDir!!)
        }

        gradlePropertiesProperty(semverProperty, projectDir).isPresent -> {
            gradlePropertiesProperty(semverProperty, projectDir)
        }

        else -> {
            providers.provider { null }
        }
    }
