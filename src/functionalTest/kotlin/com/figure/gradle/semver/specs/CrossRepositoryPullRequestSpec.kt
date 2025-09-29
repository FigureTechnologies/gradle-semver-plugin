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

class CrossRepositoryPullRequestSpec : FunSpec({
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

    test("should calculate next version for cross-repository/forked pull request") {
        withEnvironment(
            environment = mapOf(
                Env.CI to "true",
                Env.GITHUB_HEAD_REF to forkedFeatureBranch,
            ),
            mode = OverrideMode.SetOrOverride,
        ) {
            // Given: Simulate a cross-repo PR scenario where the feature branch doesn't exist locally
            // This mimics what happens when GitHub Actions checks out a forked pull request
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")

                    checkout(developmentBranch)
                    commit(message = "1 commit on $developmentBranch")

                    // Simulate being on main branch (as in cross-repo PRs) but with GITHUB_HEAD_REF set
                    // to the forked branch name that doesn't exist locally
                    checkout(mainBranch)
                }
            }

            // When: Building should not fail even though the branch name from GITHUB_HEAD_REF doesn't exist locally
            projects.build(GradleVersion.current())

            // Then: Should generate a version using the forked branch name from GITHUB_HEAD_REF
            projects.versions shouldOnlyHave "1.0.1-fix-builds-fail-cross-repo.0"
        }
    }

    test("should handle cross-repo PR with both GITHUB_HEAD_REF and GITHUB_REF_NAME set") {
        withEnvironment(
            environment = mapOf(
                Env.CI to "true",
                Env.GITHUB_HEAD_REF to forkedFeatureBranch,
                Env.GITHUB_REF_NAME to "123/merge", // Typical PR merge ref
            ),
            mode = OverrideMode.SetOrOverride,
        ) {
            // Given: Similar setup but with both environment variables set
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                    checkout(mainBranch) // Stay on main to simulate cross-repo PR checkout
                }
            }

            // When: Building should prioritize GITHUB_HEAD_REF over GITHUB_REF_NAME
            projects.build(GradleVersion.current())

            // Then: Should use the forked branch name, not the merge ref
            projects.versions shouldOnlyHave "1.0.1-fix-builds-fail-cross-repo.0"
        }
    }

    test("should fallback to GITHUB_REF_NAME when GITHUB_HEAD_REF is empty") {
        withEnvironment(
            environment = mapOf(
                Env.CI to "true",
                Env.GITHUB_HEAD_REF to "", // Empty but present
                Env.GITHUB_REF_NAME to "feature-branch-fallback",
            ),
            mode = OverrideMode.SetOrOverride,
        ) {
            // Given: GITHUB_HEAD_REF is empty (not a PR) but GITHUB_REF_NAME is set
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "1.0.0")
                    checkout("feature-branch-fallback")
                    commit(message = "1 commit on feature branch")
                }
            }

            // When: Building should use GITHUB_REF_NAME since GITHUB_HEAD_REF is empty
            projects.build(GradleVersion.current())

            // Then: Should use the ref name from GITHUB_REF_NAME with correct commit count
            projects.versions shouldOnlyHave "1.0.1-feature-branch-fallback.1"
        }
    }
})
