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

import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import org.gradle.util.GradleVersion

class CalculateNextVersionWithoutInputsSpec : FunSpec({
    val projects =
        install(
            GradleProjectsExtension(
                RegularProject(projectName = "regular-project"),
                SettingsProject(projectName = "settings-project"),
                SubprojectProject(projectName = "subproject-project"),
            ),
        )

    val mainBranch = "main"
    val developmentBranch = "develop"
    val featureBranch = "myname/sc-123456/my-awesome-feature"

    context("should calculate next version without inputs") {
        test("on main branch") {
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
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.1"
        }

        test("on development branch") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.1-develop.1"
        }

        test("on development branch with latest development tag") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch", tag = "1.0.1-develop.1")
                        commit(message = "2 commit on $developmentBranch")
                        commit(message = "3 commit on $developmentBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.1-develop.3"
        }

        test("on feature branch off development branch") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                        commit(message = "2 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.1-myname-sc-123456-my-awesome-feature.2"
        }

        test("on feature branch off main branch") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                        commit(message = "2 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.1-myname-sc-123456-my-awesome-feature.2"
        }

        test("for develop branch after committing to feature branch and switching back to develop") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")

                        checkout(developmentBranch)
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.1-develop.1"
        }

        test("next branch version where last tag is prerelease") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.2")
                        commit(message = "1 commit on $mainBranch", tag = "1.0.3-alpha.1")

                        checkout(featureBranch)
                        commit(message = "1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "1.0.3-myname-sc-123456-my-awesome-feature.1"
        }
    }
})
