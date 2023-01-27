/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.external

import com.figure.gradle.semver.internal.git.GitRef
import net.swiftzer.semver.SemVer

typealias VersionCalculatorStrategy = List<BranchMatchingConfiguration>
typealias VersionModifier = SemVer.() -> SemVer // TODO: Does this need to be a method?
typealias VersionQualifier = SemverContext.(current: GitRef.Branch) -> Pair<PreReleaseLabel, BuildMetadataLabel>

data class BranchMatchingConfiguration(
    val regex: Regex,
    val targetBranch: GitRef.Branch,
    val versionQualifier: VersionQualifier,
    val versionModifier: VersionModifier = { nextPatch() }
)

fun flowVersionCalculatorStrategy(versionModifier: VersionModifier): VersionCalculatorStrategy = listOf(
    BranchMatchingConfiguration(
        regex = """^main$""".toRegex(),
        targetBranch = GitRef.Branch.MAIN,
        versionQualifier = {
            PreReleaseLabel.EMPTY to BuildMetadataLabel.EMPTY
        },
        versionModifier = versionModifier
    ),
    BranchMatchingConfiguration(
        regex = """^develop$""".toRegex(),
        targetBranch = GitRef.Branch.MAIN,
        versionQualifier = { currentBranch ->
            preReleaseWithCommitCount(
                currentBranch = currentBranch,
                targetBranch = GitRef.Branch.MAIN,
                label = "beta"
            ) to BuildMetadataLabel.EMPTY
        },
        versionModifier = versionModifier
    ),
    BranchMatchingConfiguration(
        regex = """^hotfix/.*""".toRegex(),
        targetBranch = GitRef.Branch.MAIN,
        versionQualifier = { currentBranch ->
            preReleaseWithCommitCount(
                currentBranch = currentBranch,
                targetBranch = GitRef.Branch.MAIN,
                label = "rc"
            ) to BuildMetadataLabel.EMPTY
        },
        versionModifier = versionModifier
    ),
    /**
     * This one must be last so the other configurations get matched first
     */
    BranchMatchingConfiguration(
        regex = """.*""".toRegex(),
        targetBranch = GitRef.Branch.DEVELOP,
        versionQualifier = { currentBranch ->
            preReleaseWithCommitCount(
                currentBranch = currentBranch,
                targetBranch = GitRef.Branch.MAIN,
                label = currentBranch.sanitizedNameWithoutPrefix()
            ) to BuildMetadataLabel.EMPTY
        },
        versionModifier = versionModifier
    ),
)

fun flatVersionCalculatorStrategy(versionModifier: VersionModifier): VersionCalculatorStrategy = listOf(
    BranchMatchingConfiguration(
        regex = """^main$""".toRegex(),
        targetBranch = GitRef.Branch.MAIN,
        versionQualifier = { PreReleaseLabel.EMPTY to BuildMetadataLabel.EMPTY },
        versionModifier = versionModifier
    ),
    BranchMatchingConfiguration(
        regex = """.*""".toRegex(),
        targetBranch = GitRef.Branch.MAIN,
        versionQualifier = {
            preReleaseWithCommitCount(
                currentBranch = it,
                targetBranch = GitRef.Branch.MAIN,
                label = it.sanitizedNameWithoutPrefix()
            ) to BuildMetadataLabel.EMPTY
        },
        versionModifier = versionModifier
    ),
)
