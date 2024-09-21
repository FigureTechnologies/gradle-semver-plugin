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

import com.figure.gradle.semver.internal.properties.Modifier
import com.figure.gradle.semver.internal.properties.SemverProperty
import com.figure.gradle.semver.internal.properties.Stage
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyContain
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import com.figure.gradle.semver.util.GradleArgs.semverForMajorVersion
import com.figure.gradle.semver.util.GradleArgs.semverModifier
import com.figure.gradle.semver.util.GradleArgs.semverStage
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import org.gradle.util.GradleVersion

class CalculateNextVersionForMajorVersionSpec : FunSpec({
    val projects =
        install(
            GradleProjectsExtension(
                RegularProject(projectName = "regular-project"),
                SettingsProject(projectName = "settings-project"),
                SubprojectProject(projectName = "subproject-project"),
            ),
        )

    val mainBranch = "master"
    val developmentBranch = "devel"
    val featureBranch = "cool-feature"
    val releaseBranch = "release/v0"

    context("should not calculate next version for major version") {
        test("when value is not an integer") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(releaseBranch)
                        commit(message = "1 commit on $releaseBranch")
                    }
            }

            // When
            val outputs =
                projects.runWithoutExpectations(
                    GradleVersion.current(),
                    "-P${SemverProperty.ForMajorVersion.property}=not-an-integer",
                ).values.map { it.output }

            // Then
            outputs shouldOnlyContain "semver.forMajorVersion must be representative of a valid major version line (0, 1, 2, etc.)"
        }

        test("when modifier is major and next major version is specified") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(releaseBranch)
                        commit(message = "1 commit on $releaseBranch")
                    }
            }

            // When
            val results =
                projects.runWithoutExpectations(
                    GradleVersion.current(),
                    semverStage(Stage.Stable),
                    semverModifier(Modifier.Major),
                    semverForMajorVersion(0),
                )

            // Then
            results.values.map { it.output } shouldOnlyContain "forMajorVersion cannot be used with the 'major' modifier"
        }
    }

    context("should calculate next version for major version") {
        test("on main branch - next minor version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(mainBranch)
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Minor), semverForMajorVersion(0))

            // Then
            projects.versions shouldOnlyHave "0.3.0"
        }

        test("on main branch - next patch version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(mainBranch)
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverForMajorVersion(0))

            // Then
            projects.versions shouldOnlyHave "0.2.6"
        }

        test("on development branch - next patch version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverStage(Stage.Stable), semverForMajorVersion(0))

            // Then
            projects.versions shouldOnlyHave "0.2.6"
        }

        test("on feature branch - next patch version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverStage(Stage.Stable), semverForMajorVersion(0))

            // Then
            projects.versions shouldOnlyHave "0.2.6"
        }

        test("on feature branch - new release candidate version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.1")
                        commit(message = "1 commit on $mainBranch", tag = "1.1.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverStage(Stage.ReleaseCandidate), semverForMajorVersion(0))

            // Then
            projects.versions shouldOnlyHave "0.2.6-rc.1"
        }
    }
})
