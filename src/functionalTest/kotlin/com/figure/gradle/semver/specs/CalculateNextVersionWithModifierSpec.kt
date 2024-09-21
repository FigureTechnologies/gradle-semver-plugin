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
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyContain
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import com.figure.gradle.semver.util.GradleArgs.semverModifier
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import org.gradle.util.GradleVersion

class CalculateNextVersionWithModifierSpec : FunSpec({
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

    context("should not calculate next version") {
        test("when modifier is invalid") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(featureBranch)
                    }
            }

            // When / Then
            val results = projects.runWithoutExpectations(GradleVersion.current(), "-P${SemverProperty.Modifier.property}=invalid")

            // Then
            results.values.map { it.output } shouldOnlyContain "Invalid modifier provided"
        }
    }

    context("should calculate next version with modifier input") {
        test("on main branch - next major version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(mainBranch)
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Major))

            // Then
            projects.versions shouldOnlyHave "2.0.0"
        }

        test("on main branch - next minor version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(mainBranch)
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Minor))

            // Then
            projects.versions shouldOnlyHave "1.1.0"
        }

        test("on main branch - next patch version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(mainBranch)
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Patch))

            // Then
            projects.versions shouldOnlyHave "1.0.1"
        }

        test("on feature branch - next major version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Major))

            // Then
            projects.versions shouldOnlyHave "2.0.0-cool-feature.1"
        }

        test("on feature branch - next minor version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Minor))

            // Then
            projects.versions shouldOnlyHave "1.1.0-cool-feature.1"
        }

        test("on feature branch - next patch version") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverModifier(Modifier.Patch))

            // Then
            projects.versions shouldOnlyHave "1.0.1-cool-feature.1"
        }
    }
})
