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
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import io.github.z4kn4fein.semver.nextPatch
import io.github.z4kn4fein.semver.toVersion
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import org.gradle.util.GradleVersion

class UseInitialVersionSpec : FunSpec({
    val mainBranch = "main"
    val developmentBranch = "develop"
    val featureBranch = "patch-1"

    val initialVersion = "1.1.1"
    val expectedStableVersion = initialVersion.toVersion().nextPatch().toString()

    context("should use initial version") {
        val semver =
            semver {
                this.initialVersion = initialVersion
            }

        val projects =
            install(
                GradleProjectsExtension(
                    RegularProject(projectName = "regular-project", semver = semver),
                    SettingsProject(projectName = "settings-project", semver = semver),
                    SubprojectProject(projectName = "subproject-project", semver = semver),
                ),
            )

        test("on main branch") {
            // Given
            // The default initial value is "0.0.0" which is supplied by the plugin
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit("1 commit on $mainBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave expectedStableVersion
        }

        test("on development branch") {
            // Given
            // The default initial value is "0.0.0" which is supplied by the plugin
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit("1 commit on $mainBranch")

                        checkout(developmentBranch)
                        commit("1 commit on $developmentBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "$expectedStableVersion-develop.1"
        }

        test("on feature branch") {
            // Given
            // The default initial value is "0.0.0" which is supplied by the plugin
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit("1 commit on $mainBranch")

                        checkout(featureBranch)
                        commit("1 commit on $featureBranch")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "$expectedStableVersion-patch-1.1"
        }
    }
})
