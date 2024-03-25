/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
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

// TODO: Clean this up for v2.
//  - The end user shouldn't have to know about the SemVer object.
//  - VersionQualifier is probably more complicated than necessary.
//  - Support RC versions properly. Should this be a VersionModifier?
data class BranchMatchingConfiguration(
    val regex: Regex,
    val targetBranch: GitRef.Branch,
    val versionQualifier: VersionQualifier,
    val versionModifier: VersionModifier = { nextPatch() },
)

fun mainBasedFlowVersionCalculatorStrategy(versionModifier: VersionModifier): VersionCalculatorStrategy =
    buildFlowVersionCalculatorStrategy(GitRef.Branch.MAIN, versionModifier)

fun masterBasedFlowVersionCalculatorStrategy(versionModifier: VersionModifier): VersionCalculatorStrategy =
    buildFlowVersionCalculatorStrategy(GitRef.Branch.MASTER, versionModifier)

private fun buildFlowVersionCalculatorStrategy(
    targetBranch: GitRef.Branch,
    versionModifier: VersionModifier,
): VersionCalculatorStrategy =
    listOf(
        BranchMatchingConfiguration(
            regex = """^${targetBranch.name}$""".toRegex(),
            targetBranch = targetBranch,
            versionQualifier = {
                PreReleaseLabel.EMPTY to BuildMetadataLabel.EMPTY
            },
            versionModifier = versionModifier,
        ),
        BranchMatchingConfiguration(
            regex = """^develop$""".toRegex(),
            targetBranch = targetBranch,
            versionQualifier = { currentBranch ->
                preReleaseWithCommitCount(
                    currentBranch = currentBranch,
                    targetBranch = targetBranch,
                    label = "beta",
                ) to BuildMetadataLabel.EMPTY
            },
            versionModifier = versionModifier,
        ),
        BranchMatchingConfiguration(
            regex = """^rc/.*""".toRegex(),
            targetBranch = targetBranch,
            versionQualifier = { currentBranch ->
                preReleaseWithCommitCount(
                    currentBranch = currentBranch,
                    targetBranch = targetBranch,
                    label = "rc",
                ) to BuildMetadataLabel.EMPTY
            },
            versionModifier = versionModifier,
        ),
        // This one must be last so the other configurations get matched first
        BranchMatchingConfiguration(
            regex = """.*""".toRegex(),
            targetBranch = GitRef.Branch.DEVELOP,
            versionQualifier = { currentBranch ->
                preReleaseWithCommitCount(
                    currentBranch = currentBranch,
                    targetBranch = targetBranch,
                    label = currentBranch.sanitizedNameWithoutPrefix(),
                ) to BuildMetadataLabel.EMPTY
            },
            versionModifier = versionModifier,
        ),
    )

fun mainBasedFlatVersionCalculatorStrategy(versionModifier: VersionModifier): VersionCalculatorStrategy =
    buildFlatVersionCalculatorStrategy(GitRef.Branch.MAIN, versionModifier)

fun masterBasedFlatVersionCalculatorStrategy(versionModifier: VersionModifier): VersionCalculatorStrategy =
    buildFlatVersionCalculatorStrategy(GitRef.Branch.MASTER, versionModifier)

private fun buildFlatVersionCalculatorStrategy(
    targetBranch: GitRef.Branch,
    versionModifier: VersionModifier,
): VersionCalculatorStrategy =
    listOf(
        BranchMatchingConfiguration(
            regex = """^${targetBranch.name}$""".toRegex(),
            targetBranch = targetBranch,
            versionQualifier = { PreReleaseLabel.EMPTY to BuildMetadataLabel.EMPTY },
            versionModifier = versionModifier,
        ),
        BranchMatchingConfiguration(
            regex = """^rc/.*""".toRegex(),
            targetBranch = targetBranch,
            versionQualifier = { currentBranch ->
                preReleaseWithCommitCount(
                    currentBranch = currentBranch,
                    targetBranch = targetBranch,
                    label = "rc",
                ) to BuildMetadataLabel.EMPTY
            },
            versionModifier = versionModifier,
        ),
        BranchMatchingConfiguration(
            regex = """.*""".toRegex(),
            targetBranch = targetBranch,
            versionQualifier = {
                preReleaseWithCommitCount(
                    currentBranch = it,
                    targetBranch = targetBranch,
                    label = it.sanitizedNameWithoutPrefix(),
                ) to BuildMetadataLabel.EMPTY
            },
            versionModifier = versionModifier,
        ),
    )
