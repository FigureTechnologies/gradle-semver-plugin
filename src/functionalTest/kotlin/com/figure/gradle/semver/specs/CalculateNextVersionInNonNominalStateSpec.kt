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

import com.figure.gradle.semver.git.Script
import com.figure.gradle.semver.internal.command.GitState
import com.figure.gradle.semver.kotest.GradleProjectsExtension
import com.figure.gradle.semver.kotest.shouldOnlyHave
import com.figure.gradle.semver.projects.RegularProject
import com.figure.gradle.semver.projects.SettingsProject
import com.figure.gradle.semver.projects.SubprojectProject
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import org.gradle.util.GradleVersion

class CalculateNextVersionInNonNominalStateSpec : FunSpec({
    val projects = install(
        GradleProjectsExtension(
            RegularProject(projectName = "regular-project"),
            SettingsProject(projectName = "settings-project"),
            SubprojectProject(projectName = "subproject-project"),
        ),
    )

    val mainBranch = "main"
    val featureBranch = "feature-branch"

    context("should calculate next version in non nominal state when") {
        withData(
            nameFn = { "running ${it.script.scriptFileName}" },
            TestData(Script.CREATE_BISECTING_STATE, "0.2.6-${GitState.BISECTING.description}"),
            TestData(Script.CREATE_CHERRY_PICKING_STATE, "0.2.6-${GitState.CHERRY_PICKING.description}"),
            TestData(Script.CREATE_MERGING_STATE, "0.2.6-${GitState.MERGING.description}"),
            TestData(Script.CREATE_REBASING_STATE, "0.2.6-${GitState.REBASING.description}"),
            TestData(Script.CREATE_REVERTING_STATE, "0.2.6-${GitState.REVERTING.description}"),
            TestData(Script.CREATE_DETACHED_HEAD_STATE, "0.2.6-${GitState.DETACHED_HEAD.description}"),
        ) {
            // Given
            projects.git {
                initialBranch = mainBranch
                actions = actions {
                    commit(message = "1 commit on $mainBranch", tag = "0.2.5")
                    runScript(it.script, mainBranch, featureBranch)
                }
            }

            // When
            projects.build(GradleVersion.current())

            // Then
            projects.versions shouldOnlyHave it.expectedVersion
        }
    }
})

private data class TestData(
    val script: Script,
    val expectedVersion: String,
)
