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
package com.figure.gradle.semver.internal.calculator

import com.figure.gradle.semver.internal.command.KGit
import com.figure.gradle.semver.internal.extensions.appendBuildMetadata
import com.figure.gradle.semver.internal.extensions.prereleaseLabel
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.inc

/**
 * Should calculate the next version based on the current branch name
 */
class BranchVersionCalculator(
    private val kGit: KGit,
) : VersionCalculator {
    override fun calculate(latestVersion: Version, context: VersionCalculatorContext): String = with(context) {
        val currentBranch = kGit.branch.currentRef
        val developmentBranch = kGit.branches.findDevelopmentBranch(developmentBranch, mainBranch)
        val mainBranch = kGit.branches.findMainBranch(mainBranch)

        val commitCount = if (currentBranch != developmentBranch) {
            kGit.branches.commitCountBetween(developmentBranch.name, currentBranch.name)
        } else {
            kGit.branches.commitCountBetween(mainBranch.name, currentBranch.name)
        }

        val prereleaseLabel = currentBranch.prereleaseLabel()
        val prereleaseLabelWithCommitCount = "$prereleaseLabel.$commitCount"

        return latestVersion
            .inc(modifier.toInc(), prereleaseLabelWithCommitCount)
            .appendBuildMetadata(context.appendBuildMetadata)
            .toString()
    }
}
