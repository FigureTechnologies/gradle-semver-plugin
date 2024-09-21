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
import com.figure.gradle.semver.util.GradleArgs.semverTagPrefix
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import org.gradle.util.GradleVersion

class UseTagPrefixSpec : FunSpec({
    val projects =
        install(
            GradleProjectsExtension(
                RegularProject(projectName = "regular-project"),
                SettingsProject(projectName = "settings-project"),
                SubprojectProject(projectName = "subproject-project"),
            ),
        )

    val mainBranch = "main"

    context("should use tag prefix") {
        test("on main branch with default tag prefix") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                    }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave "0.2.6"
            projects.versionTags shouldOnlyHave "v0.2.6"
        }

        test("on feature branch with provided tag prefix") {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions =
                    actions {
                        commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                    }
            }

            // When
            projects.build(GradleVersion.current(), semverTagPrefix("Nov Release "))

            // Then
            projects.versions shouldOnlyHave "0.2.6"
            projects.versionTags shouldOnlyHave "Nov Release 0.2.6"
        }
    }
})
