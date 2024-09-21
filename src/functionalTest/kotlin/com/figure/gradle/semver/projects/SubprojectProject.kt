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
package com.figure.gradle.semver.projects

import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.SettingsScript
import com.figure.gradle.semver.Constants
import com.figure.gradle.semver.gradle.SemverConfiguration
import com.figure.gradle.semver.gradle.buildCache
import com.figure.gradle.semver.gradle.local
import com.figure.gradle.semver.gradle.semver
import com.figure.gradle.semver.gradle.settingsGradle
import com.figure.gradle.semver.plugins.GradlePlugins
import java.util.Properties

class SubprojectProject(
    override val projectName: String,
    private val semver: SemverConfiguration = semver {},
) : AbstractProject() {
    override val gradleProject: GradleProject
        get() = build()

    private val subprojectName = "subproj"

    private fun build(): GradleProject =
        newGradleProjectBuilder(dslKind).withRootProject {
            withBuildScript {
                plugins(GradlePlugins.kotlinNoApply)
            }

            val settings =
                settingsGradle {
                    buildCache =
                        buildCache {
                            local =
                                local {
                                    directory = buildCacheDir
                                }
                        }
                }

            settingsScript =
                SettingsScript(
                    additions = scribe.use { s -> settings.render(s) },
                )
        }.withSubproject(subprojectName) {
            withBuildScript {
                plugins(GradlePlugins.semverPlugin, GradlePlugins.kotlinNoApply)
                additions = scribe.use { s -> semver.render(s) }
            }
        }.write()

    override fun fetchSemverProperties(): Properties =
        gradleProject.projectDir(subprojectName).toFile().resolve(Constants.SEMVER_PROPERTY_PATH).let {
            Properties().apply { load(it.reader()) }
        }
}
