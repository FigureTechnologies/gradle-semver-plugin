/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.semver

import com.figure.gradle.semver.external.ContextProviderOperations
import com.figure.gradle.semver.external.SemverContext
import com.figure.gradle.semver.external.VersionModifier
import com.figure.gradle.semver.internal.exceptions.MissingBranchMatchingConfigurationException
import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.internal.semverError
import com.figure.gradle.semver.internal.semverInfo
import com.figure.gradle.semver.internal.semverWarn
import net.swiftzer.semver.SemVer
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

internal interface VersionCalculator {
    fun calculateVersion(): Result<SemVer>
}

internal class TargetBranchVersionCalculator(
    private val contextProviderOperations: ContextProviderOperations,
    private val config: VersionCalculatorConfig,
    private val context: SemverContext,
    private val currentBranch: GitRef.Branch
) : VersionCalculator {

    override fun calculateVersion(): Result<SemVer> {
        return previousVersion().map {
            versionQualifier(versionModifier(it))
        }
    }

    private fun previousVersion(): Result<SemVer> {
        return config.branchMatching
            .firstOrNull { it.regex.matches(currentBranch.name) }
            ?.let { bmc ->
                log.semverInfo("Using BranchMatchingConfiguration: $bmc for previousVersion() with currentBranch: $currentBranch")
                contextProviderOperations.branchVersion(currentBranch, bmc.targetBranch).map { semver ->
                    log.semverInfo("Branch version for current $currentBranch and target ${bmc.targetBranch}: $semver")
                    semver ?: run {
                        log.semverWarn("No version found for target branch ${bmc.targetBranch}, using initial version")
                        config.initialVersion
                    }
                }
            }
            ?: run {
                log.semverWarn("No match found for $currentBranch in ${config.branchMatching}, using initial version as previous version")
                Result.failure(MissingBranchMatchingConfigurationException(currentBranch))
            }
    }

    private fun versionModifier(current: SemVer): SemVer {
        return config.branchMatching
            .firstOrNull { it.regex.matches(currentBranch.name) }
            ?.let { bmc ->
                log.semverInfo("Using BranchMatchingConfiguration: $bmc for versionModifier() with currentBranch: $currentBranch")
                val fn = bmc.versionModifier
                current.fn()
            }
            ?: run {
                log.semverWarn("No match found for $currentBranch in ${config.branchMatching}, using initial version as modified version")
                config.initialVersion
            }
    }

    private fun versionQualifier(current: SemVer): SemVer {
        return config.branchMatching
            .firstOrNull { it.regex.matches(currentBranch.name) }
            ?.let { bmc ->
                log.semverInfo("Using BranchMatchingConfiguration: $bmc for versionQualifier() with currentBranch $currentBranch")
                val fn = bmc.versionQualifier
                context.fn(currentBranch).let {
                    current.copy(
                        preRelease = it.first.value.ifBlank { null },
                        buildMetadata = it.second.value.ifBlank { null }
                    )
                }
            }
            ?: run {
                log.semverWarn("No match found for $currentBranch in ${config.branchMatching}")
                current
            }
    }
}

internal fun versionModifierFromString(modifier: String): VersionModifier {
    return when (val mod = modifier.trim().lowercase()) {
        "major" -> SemVer::nextMajor
        "minor" -> SemVer::nextMinor
        "patch" -> SemVer::nextPatch
        else -> {
            log.semverError("Unknown version modifier [$mod]")
            throw Exception("Unknown version modifier [$modifier]")
        }
    }
}
