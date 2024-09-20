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

import com.figure.gradle.semver.internal.properties.BuildMetadataOptions
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.kotest.shouldOnlyMatch
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import com.figure.gradle.semver.util.GradleArgs.semverAppendBuildMetadata
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import org.gradle.util.GradleVersion

class UseAppendBuildMetadataPropertySpec : FunSpec({
    val projects = install(
        GradleProjectsExtension(
            RegularProject(projectName = "regular-project"),
            SettingsProject(projectName = "settings-project"),
            SubprojectProject(projectName = "subproject-project"),
        ),
    )

    val mainBranch = "main"
    val developmentBranch = "develop"
    val featureBranch = "patch-1"

    context("should not use override version") {
        test("when override version is invalid") {
            // Given
            val appendBuildMetadataOption = "invalid"

            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                }
            }

            // When
            projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

            // Then
            projects.versions shouldOnlyHave "1.0.1"
        }

        test("when ${BuildMetadataOptions.NEVER} is specified") {
            // Given
            val appendBuildMetadataOption = BuildMetadataOptions.NEVER.name

            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                }
            }

            // When
            projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

            // Then
            projects.versions shouldOnlyHave "1.0.1"
        }

        test("when ${BuildMetadataOptions.LOCALLY} is specified but building in CI") {
            withEnvironment("CI", "true", mode = OverrideMode.SetOrOverride) {
                // Given
                val appendBuildMetadataOption = BuildMetadataOptions.LOCALLY.name

                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                    }
                }

                // When
                projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                // Then
                projects.versions shouldOnlyHave "1.0.1"
            }
        }
    }

    context("should append build metadata") {
        context("when ${BuildMetadataOptions.ALWAYS} is specified") {
            val appendBuildMetadataOption = BuildMetadataOptions.ALWAYS.name

            test("and on $mainBranch branch") {
                // Given
                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                    }
                }

                // When
                projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                // Then
                projects.versions shouldOnlyMatch """1.0.1\+[0-9]{12}""".toRegex()
            }

            test("and on $developmentBranch branch") {
                // Given
                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")
                    }
                }

                // When
                projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                // Then
                projects.versions shouldOnlyMatch """1.0.1-$developmentBranch.1\+[0-9]{12}""".toRegex()
            }

            test("and on $featureBranch branch") {
                // Given
                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
                }

                // When
                projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                // Then
                projects.versions shouldOnlyMatch """1.0.1-${featureBranch.replace("/", "-")}.1\+[0-9]{12}""".toRegex()
            }
        }

        context("when ${BuildMetadataOptions.LOCALLY} is specified") {
            val appendBuildMetadataOption = BuildMetadataOptions.LOCALLY.name

            test("and on $mainBranch branch") {
                withEnvironment("CI", null, mode = OverrideMode.SetOrOverride) {
                    // Given
                    projects.git {
                        initialBranch = mainBranch
                        actions = actions {
                            commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        }
                    }

                    // When
                    projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                    // Then
                    projects.versions shouldOnlyMatch """1.0.1\+[0-9]{12}""".toRegex()
                }
            }

            test("and on $developmentBranch branch") {
                withEnvironment("CI", null, mode = OverrideMode.SetOrOverride) {
                    // Given
                    projects.git {
                        initialBranch = mainBranch
                        actions = actions {
                            commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                            checkout(developmentBranch)
                            commit(message = "1 commit on $developmentBranch")
                        }
                    }

                    // When
                    projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                    // Then
                    projects.versions shouldOnlyMatch """1.0.1-$developmentBranch.1\+[0-9]{12}""".toRegex()
                }
            }

            test("and on $featureBranch branch") {
                withEnvironment("CI", null, mode = OverrideMode.SetOrOverride) {
                    // Given
                    projects.git {
                        initialBranch = mainBranch
                        actions = actions {
                            commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                            checkout(featureBranch)
                            commit(message = "1 commit on $featureBranch")
                        }
                    }

                    // When
                    projects.build(GradleVersion.current(), semverAppendBuildMetadata(appendBuildMetadataOption))

                    // Then
                    projects.versions shouldOnlyMatch """1.0.1-${featureBranch.replace("/", "-")}.1\+[0-9]{12}""".toRegex()
                }
            }
        }
    }
})
