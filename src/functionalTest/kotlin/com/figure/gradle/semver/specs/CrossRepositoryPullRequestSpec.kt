/*
 * Copyright (C) 2024-2026 Figure Technologies
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

import com.figure.gradle.semver.internal.environment.Env
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import org.gradle.util.GradleVersion

class CrossRepositoryPullRequestSpec :
    FunSpec({
        val projects = install(
            GradleProjectsExtension(
                RegularProject(projectName = "regular-project"),
                SettingsProject(projectName = "settings-project"),
                SubprojectProject(projectName = "subproject-project"),
            ),
        )

        val mainBranch = "main"
        val developmentBranch = "develop"
        val forkedFeatureBranch = "fix/builds-fail-cross-repo"

        test("should calculate next version when cross-repo pull request has no local head branch") {
            withEnvironment(
                environment = mapOf(
                    Env.CI to "true",
                    Env.GITHUB_HEAD_REF to forkedFeatureBranch,
                ),
                mode = OverrideMode.SetOrOverride,
            ) {
                // Given
                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                        checkout(developmentBranch)
                        commit(message = "1 commit on $developmentBranch")

                        // Stay on main: fork branch name exists only in GITHUB_HEAD_REF
                        checkout(mainBranch)
                    }
                }

                // When
                projects.build(GradleVersion.current())

                // Then
                projects.versions shouldOnlyHave "1.0.1-fix-builds-fail-cross-repo.0"
            }
        }

        test("should prefer GITHUB_HEAD_REF when GITHUB_REF_NAME is merge ref") {
            withEnvironment(
                environment = mapOf(
                    Env.CI to "true",
                    Env.GITHUB_HEAD_REF to forkedFeatureBranch,
                    Env.GITHUB_REF_NAME to "123/merge",
                ),
                mode = OverrideMode.SetOrOverride,
            ) {
                // Given
                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        checkout(mainBranch)
                    }
                }

                // When
                projects.build(GradleVersion.current())

                // Then
                projects.versions shouldOnlyHave "1.0.1-fix-builds-fail-cross-repo.0"
            }
        }

        test("should fall back to GITHUB_REF_NAME when GITHUB_HEAD_REF is empty") {
            withEnvironment(
                environment = mapOf(
                    Env.CI to "true",
                    Env.GITHUB_HEAD_REF to "",
                    Env.GITHUB_REF_NAME to "feature-branch-fallback",
                ),
                mode = OverrideMode.SetOrOverride,
            ) {
                // Given
                projects.git {
                    initialBranch = mainBranch
                    actions = actions {
                        commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                        checkout("feature-branch-fallback")
                        commit(message = "1 commit on feature branch")
                    }
                }

                // When
                projects.build(GradleVersion.current())

                // Then
                projects.versions shouldOnlyHave "1.0.1-feature-branch-fallback.1"
            }
        }
    })
