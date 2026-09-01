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
package com.figure.gradle.semver.specs

import com.figure.gradle.semver.gradle.semver
import com.figure.gradle.semver.internal.properties.BuildMetadataOptions
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.kotest.shouldOnlyMatch
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

class ConfigurationCacheReuseSpec : FunSpec({
    val mainBranch = "main"
    val developmentBranch = "develop"
    val featureBranch = "feature/cc-reuse"

    fun Map<*, BuildResult>.shouldReuseConfigurationCache() {
        values.forEach { result ->
            result.output shouldContain "Reusing configuration cache"
        }
    }

    fun Map<*, BuildResult>.shouldStoreConfigurationCache() {
        values.forEach { result ->
            result.output shouldContain "Configuration cache entry stored"
            result.output shouldNotContain "Reusing configuration cache"
        }
    }

    context("should reuse configuration cache when version changes") {
        test("when a new commit is added on a feature branch") {
            // Given
            val projects = install(
                GradleProjectsExtension(
                    RegularProject(projectName = "regular-project"),
                    SettingsProject(projectName = "settings-project"),
                ),
            )
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                    checkout(developmentBranch)
                    commit(message = "1 commit on $developmentBranch")
                    checkout(featureBranch)
                    commit(message = "1 commit on $featureBranch")
                }
            }

            // When
            val firstBuild = projects.build(GradleVersion.current())

            // Then
            firstBuild.shouldStoreConfigurationCache()
            projects.versions shouldOnlyHave "1.0.1-feature-cc-reuse.1"

            // Given
            projects.commit(message = "2 commit on $featureBranch")

            // When
            val secondBuild = projects.build(GradleVersion.current())

            // Then
            secondBuild.shouldReuseConfigurationCache()
            projects.versions shouldOnlyHave "1.0.1-feature-cc-reuse.2"
        }
    }

    context("should reuse configuration cache when version is unchanged") {
        test("when a new commit is added on the main branch") {
            // Given
            val projects = install(
                GradleProjectsExtension(
                    RegularProject(projectName = "regular-project"),
                    SettingsProject(projectName = "settings-project"),
                ),
            )
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                    checkout(developmentBranch)
                    commit(message = "1 commit on $developmentBranch")
                    checkout(mainBranch)
                }
            }

            // When
            val firstBuild = projects.build(GradleVersion.current())

            // Then
            firstBuild.shouldStoreConfigurationCache()
            projects.versions shouldOnlyHave "1.0.1"

            // Given
            projects.commit(message = "2 commit on $mainBranch")

            // When
            val secondBuild = projects.build(GradleVersion.current())

            // Then
            secondBuild.shouldReuseConfigurationCache()
            projects.versions shouldOnlyHave "1.0.1"
        }
    }

    context("should reuse configuration cache when build metadata timestamp changes") {
        test("when appendBuildMetadata is always") {
            // Given
            val semver = semver {
                appendBuildMetadata = BuildMetadataOptions.ALWAYS.name
            }
            val projects = install(
                GradleProjectsExtension(
                    RegularProject(projectName = "regular-project", semver = semver),
                    SettingsProject(projectName = "settings-project", semver = semver),
                ),
            )
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                }
            }

            // When
            val firstBuild = projects.build(GradleVersion.current())

            // Then
            firstBuild.shouldStoreConfigurationCache()
            projects.versions shouldOnlyMatch """1.0.1\+[0-9]{14}""".toRegex()

            // Given
            Thread.sleep(1_100)

            // When
            val secondBuild = projects.build(GradleVersion.current())

            // Then
            secondBuild.shouldReuseConfigurationCache()
            projects.versions shouldOnlyMatch """1.0.1\+[0-9]{14}""".toRegex()
        }
    }
})
