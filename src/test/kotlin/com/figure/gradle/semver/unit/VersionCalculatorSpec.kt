/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.unit

import com.figure.gradle.semver.external.BranchMatchingConfiguration
import com.figure.gradle.semver.external.BuildMetadataLabel
import com.figure.gradle.semver.external.ContextProviderOperations
import com.figure.gradle.semver.external.PreReleaseLabel
import com.figure.gradle.semver.external.SemverContext
import com.figure.gradle.semver.external.VersionModifier
import com.figure.gradle.semver.external.flatVersionCalculatorStrategy
import com.figure.gradle.semver.external.flowVersionCalculatorStrategy
import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.internal.semver.TargetBranchVersionCalculator
import com.figure.gradle.semver.internal.semver.VersionCalculatorConfig
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer

class VersionCalculatorSpec : WordSpec({
    fun calculateBranchVersion(
        currentBranch: GitRef.Branch,
        branchVersions: Map<GitRef.Branch, SemVer>,
        config: VersionCalculatorConfig,
        commitsSinceBranchPoint: Int = 2,
    ): Result<SemVer> {
        val ops = getMockContextProviderOperations(currentBranch, branchVersions, commitsSinceBranchPoint)
        val context = mockSemVerContext(ops)
        val calculator = TargetBranchVersionCalculator(ops, config, context, currentBranch)
        return calculator.calculateVersion()
    }

    "Calculate version with FlatDefaultBranchMatching" should {
        "calculate the next version correctly" {
            val mainBranchVersion = SemVer(1, 2, 3)
            val mainBranch = GitRef.Branch.MAIN
            val developBranchVersion = SemVer(1, 2, 4, "beta")
            val developBranch = GitRef.Branch.DEVELOP
            val config = buildPluginConfig(flatVersionCalculatorStrategy { nextPatch() })

            val branchVersions: Map<GitRef.Branch, SemVer> = mapOf(
                mainBranch to mainBranchVersion,
                developBranch to developBranchVersion
            )

            // current == MAIN
            calculateBranchVersion(
                currentBranch = mainBranch,
                branchVersions = branchVersions,
                config = config
            ).getOrThrow() shouldBe mainBranchVersion.nextPatch()

            // current != MAIN
            setOf(
                GitRef.Branch.DEVELOP,
                GitRef.Branch("feature/my_weird_feature"),
                GitRef.Branch("something"),
                GitRef.Branch("hotfix/fix-1"),
            ).forEach { currentBranch ->
                calculateBranchVersion(
                    currentBranch = currentBranch,
                    branchVersions = branchVersions,
                    config = config
                ).getOrThrow().shouldBe(
                    mainBranchVersion
                        .nextPatch()
                        .copy(preRelease = "${currentBranch.sanitizedNameWithoutPrefix()}.2")
                )
            }
        }
    }

    "Calculate version with FlowDefaultBranchMatching" should {
        "calculate the next version correctly" {
            val mainBranchVersion = SemVer(1, 2, 3)
            val mainBranch = GitRef.Branch.MAIN
            val developBranchVersion = SemVer(1, 2, 4, "beta")
            val developBranch = GitRef.Branch.DEVELOP
            val config = buildPluginConfig(flowVersionCalculatorStrategy { nextPatch() })

            val branchVersions: Map<GitRef.Branch, SemVer> = mapOf(
                mainBranch to mainBranchVersion,
                developBranch to developBranchVersion
            )

            // current == MAIN
            calculateBranchVersion(
                currentBranch = mainBranch,
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion.nextPatch()
            )

            calculateBranchVersion(
                currentBranch = developBranch,
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .copy(preRelease = "beta.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("hotfix/something"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .copy(preRelease = "rc.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("feature/something_something*bla"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .nextPatch()
                    .copy(preRelease = "something_something-bla.2")
            )

            // Assert that any branch name gets matched properly (previously only feature/ was allowed)
            calculateBranchVersion(
                currentBranch = GitRef.Branch("something_something*bla"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .nextPatch()
                    .copy(preRelease = "something_something-bla.2")
            )
        }

        "support custom formats such as ShortCut" {
            val mainBranchVersion = SemVer(1, 2, 3)
            val mainBranch = GitRef.Branch.MAIN
            val developBranchVersion = SemVer(1, 2, 4, "beta")
            val developBranch = GitRef.Branch.DEVELOP
            val versionModifier: VersionModifier = { nextPatch() }

            val config = buildPluginConfig(
                listOf(
                    BranchMatchingConfiguration(
                        """^main$""".toRegex(),
                        GitRef.Branch.MAIN,
                        { PreReleaseLabel.EMPTY to BuildMetadataLabel.EMPTY },
                        versionModifier
                    ),
                    BranchMatchingConfiguration(
                        """^develop$""".toRegex(),
                        GitRef.Branch.MAIN,
                        { preReleaseWithCommitCount(it, GitRef.Branch.MAIN, "beta") to BuildMetadataLabel.EMPTY },
                        versionModifier
                    ),
                    BranchMatchingConfiguration(
                        """^feature/.*""".toRegex(),
                        GitRef.Branch.DEVELOP,
                        { current ->
                            preReleaseWithCommitCount(
                                currentBranch = current,
                                targetBranch = GitRef.Branch.MAIN,
                                label = current.sanitizedNameWithoutPrefix()
                            ) to BuildMetadataLabel.EMPTY
                        },
                        versionModifier
                    ),
                    BranchMatchingConfiguration(
                        """^.+/sc-\d+/.+""".toRegex(),
                        GitRef.Branch.DEVELOP,
                        { current ->
                            preReleaseWithCommitCount(
                                currentBranch = current,
                                targetBranch = GitRef.Branch.MAIN,
                                label = current.sanitizedNameWithoutPrefix()
                            ) to BuildMetadataLabel.EMPTY
                        },
                        versionModifier
                    ),
                    BranchMatchingConfiguration(
                        """^.+/\d+/.+""".toRegex(),
                        GitRef.Branch.DEVELOP,
                        { current ->
                            preReleaseWithCommitCount(
                                currentBranch = current,
                                targetBranch = GitRef.Branch.MAIN,
                                label = current.sanitizedNameWithoutPrefix()
                            ) to BuildMetadataLabel.EMPTY
                        },
                        versionModifier
                    ),
                    BranchMatchingConfiguration(
                        """^.+/no-ticket/.+""".toRegex(),
                        GitRef.Branch.DEVELOP,
                        { current ->
                            preReleaseWithCommitCount(
                                currentBranch = current,
                                targetBranch = GitRef.Branch.MAIN,
                                label = current.sanitizedNameWithoutPrefix()
                            ) to BuildMetadataLabel.EMPTY
                        },
                        versionModifier
                    ),
                    BranchMatchingConfiguration(
                        """^hotfix/.*""".toRegex(),
                        GitRef.Branch.MAIN,
                        { preReleaseWithCommitCount(it, GitRef.Branch.MAIN, "rc") to BuildMetadataLabel.EMPTY },
                        versionModifier
                    ),
                )
            )

            val branchVersions: Map<GitRef.Branch, SemVer> = mapOf(
                mainBranch to mainBranchVersion,
                developBranch to developBranchVersion
            )

            // current == MAIN
            calculateBranchVersion(
                currentBranch = mainBranch,
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion.nextPatch()
            )

            calculateBranchVersion(
                currentBranch = developBranch,
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .copy(preRelease = "beta.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("hotfix/something"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .copy(preRelease = "rc.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("feature/something_something*bla"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .nextPatch()
                    .copy(preRelease = "something_something-bla.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("someuser/sc-145300/standardized-gradle-build"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .nextPatch()
                    .copy(preRelease = "sc-145300-standardized-gradle-build.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("someuser/no-ticket/standardized-gradle-build"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .nextPatch()
                    .copy(preRelease = "no-ticket-standardized-gradle-build.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("someuser/145300/standardized-gradle-build"),
                branchVersions = branchVersions,
                config = config
            ).getOrThrow().shouldBe(
                mainBranchVersion
                    .nextPatch()
                    .nextPatch()
                    .copy(preRelease = "145300-standardized-gradle-build.2")
            )

            calculateBranchVersion(
                currentBranch = GitRef.Branch("someuser/abc/standardized-gradle-build"),
                branchVersions = branchVersions,
                config = config
            ).isFailure shouldBe true
        }
    }
})

private fun getMockContextProviderOperations(
    currentBranch: GitRef.Branch,
    branchVersion: Map<GitRef.Branch, SemVer>,
    commitsSinceBranchPoint: Int = 2,
): ContextProviderOperations = object : ContextProviderOperations {
    override fun currentBranch(): GitRef.Branch {
        return currentBranch
    }

    override fun branchVersion(
        currentBranch: GitRef.Branch,
        targetBranch: GitRef.Branch,
    ): Result<SemVer?> {
        return Result.success(
            branchVersion
                .filter { it.key.name == targetBranch.name }
                .toList()
                .firstOrNull()
                ?.second
        )
    }

    override fun commitsSinceBranchPoint(
        currentBranch: GitRef.Branch,
        targetBranch: GitRef.Branch,
    ): Result<Int> {
        return Result.success(commitsSinceBranchPoint)
    }
}

private fun buildPluginConfig(
    branchMatching: List<BranchMatchingConfiguration>,
): VersionCalculatorConfig {
    return VersionCalculatorConfig(
        "v",
        initialVersion = SemVer(0, 0, 1),
        branchMatching = branchMatching,
    )
}

private fun mockSemVerContext(
    ops: ContextProviderOperations,
): SemverContext {
    return object : SemverContext {
        override val ops: ContextProviderOperations
            get() = ops
    }
}
