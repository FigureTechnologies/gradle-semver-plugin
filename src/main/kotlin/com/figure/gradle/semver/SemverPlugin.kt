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
package com.figure.gradle.semver

import com.figure.gradle.semver.internal.calculator.VersionFactoryContext
import com.figure.gradle.semver.internal.calculator.versionFactory
import com.figure.gradle.semver.internal.extensions.extensions
import com.figure.gradle.semver.internal.extensions.providers
import com.figure.gradle.semver.internal.extensions.rootDir
import com.figure.gradle.semver.internal.logging.registerPostBuildVersionLogMessage
import com.figure.gradle.semver.internal.properties.BuildMetadataOptions
import com.figure.gradle.semver.internal.properties.appendBuildMetadata
import com.figure.gradle.semver.internal.properties.forMajorVersion
import com.figure.gradle.semver.internal.properties.modifier
import com.figure.gradle.semver.internal.properties.overrideVersion
import com.figure.gradle.semver.internal.properties.stage
import com.figure.gradle.semver.internal.properties.tagPrefix
import com.figure.gradle.semver.internal.writer.writeVersionToPropertiesFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware
import org.gradle.kotlin.dsl.create

class SemverPlugin : Plugin<PluginAware> {
    override fun apply(target: PluginAware) {
        val semverExtension = target.extensions.create<SemverExtension>("semver").apply {
            initialVersion.convention("0.0.0")
            appendBuildMetadata.convention("")
        }

        when (target) {
            is Settings -> {
                target.gradle.settingsEvaluated {
                    val nextVersion = target.calculateVersion(semverExtension)
                    target.gradle.beforeProject {
                        it.version = nextVersion
                    }
                }
            }

            is Project -> {
                target.afterEvaluate {
                    val nextVersion = target.calculateVersion(semverExtension)
                    target.version = nextVersion
                }
            }

            else -> error("Not a project or settings")
        }
    }

    private fun PluginAware.calculateVersion(semverExtension: SemverExtension): String {
        val versionFactoryContext = VersionFactoryContext(
            initialVersion = semverExtension.initialVersion.get(),
            stage = this.stage.get(),
            modifier = this.modifier.get(),
            overrideVersion = this.overrideVersion.orNull,
            forMajorVersion = this.forMajorVersion.orNull,
            rootDir = semverExtension.rootProjectDir.getOrElse { this.rootDir }.asFile,
            mainBranch = semverExtension.mainBranch.orNull,
            developmentBranch = semverExtension.developmentBranch.orNull,
            appendBuildMetadata = (appendBuildMetadata.takeIf { it.isPresent } ?: semverExtension.appendBuildMetadata)
                .map { BuildMetadataOptions.from(it, BuildMetadataOptions.NEVER) }
                .get(),
        )

        val nextVersion = this.providers.versionFactory(versionFactoryContext).get()

        this.registerPostBuildVersionLogMessage(nextVersion)
        this.writeVersionToPropertiesFile(nextVersion, tagPrefix.get())

        return nextVersion
    }
}
