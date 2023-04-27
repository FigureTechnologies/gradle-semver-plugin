/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.valuesources

import com.figure.gradle.semver.external.BranchMatchingConfiguration
import com.figure.gradle.semver.external.VersionModifier
import com.figure.gradle.semver.external.flatVersionCalculatorStrategy
import com.figure.gradle.semver.external.flowVersionCalculatorStrategy
import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.internal.git.hasBranch
import com.figure.gradle.semver.internal.semver.GitContextProviderOperations
import com.figure.gradle.semver.internal.semver.GradleSemverContext
import com.figure.gradle.semver.internal.semver.TargetBranchVersionCalculator
import com.figure.gradle.semver.internal.semver.VersionCalculatorConfig
import com.figure.gradle.semver.internal.semverError
import com.figure.gradle.semver.internal.semverInfo
import com.figure.gradle.semver.internal.semverLifecycle
import com.figure.gradle.semver.internal.util.MemoizedGit
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.api.Git
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

internal fun ProviderFactory.gitCalculateSemverProvider(
    gitDir: Property<String>,
    tagPrefix: Property<String>,
    initialVersion: String,
    overrideVersion: String,
    versionStrategy: ListProperty<BranchMatchingConfiguration>,
    versionModifier: Property<VersionModifier>,
): Provider<String> {
    return of(GitCalculateSemverValueSource::class.java) { spec ->
        spec.parameters {
            it.gitDir.set(gitDir)
            it.tagPrefix.set(tagPrefix)
            it.initialVersion.set(initialVersion)
            it.overrideVersion.set(overrideVersion)
            it.versionStrategy.set(versionStrategy)
            it.versionModifier.set(versionModifier)
        }
    }
}

internal abstract class GitCalculateSemverValueSource : ValueSource<String, GitCalculateSemverValueSource.Params> {
    interface Params : ValueSourceParameters {
        val gitDir: Property<String>
        val tagPrefix: Property<String>
        val initialVersion: Property<String>
        val overrideVersion: Property<String>
        val versionStrategy: ListProperty<BranchMatchingConfiguration>
        val versionModifier: Property<VersionModifier>
    }

    override fun obtain(): String? = calculateVersion().toString()

    private fun calculateVersion(): SemVer {
        val gitDir = parameters.gitDir.get()

        log.semverInfo("Using git directory: $gitDir")

        val git = MemoizedGit.open(gitDir)
        val config = buildCalculatorConfig(git)
        val ops = GitContextProviderOperations(git, config)
        val context = GradleSemverContext(ops)

        return config.overrideVersion ?: run {
            ops.currentBranch()?.let { currentBranch ->
                log.semverInfo("Current branch: $currentBranch")
                log.semverInfo("Semver configuration while calculating version: $config")

                val calculator = TargetBranchVersionCalculator(ops, config, context, currentBranch)
                calculator.calculateVersion().getOrElse {
                    log.semverError("Failed to calculate version", it)
                    throw Exception(it)
                }
            } ?: run {
                log.semverError("Failed to find current branch, cannot calculate semver")
                throw Exception("Failed to find current branch")
            }
        }.also {
            log.semverLifecycle("$it")
        }
    }

    private fun buildCalculatorConfig(git: Git): VersionCalculatorConfig {
        val initialConfig = VersionCalculatorConfig(
            tagPrefix = parameters.tagPrefix.get(),
            initialVersion = SemVer.parse(parameters.initialVersion.get()),
            overrideVersion = parameters.overrideVersion.orNull?.let { SemVer.parse(it) }
        )

        val versionStrategy = parameters.versionStrategy
        val versionModifier = parameters.versionModifier
        return when {
            versionStrategy.isPresent -> {
                log.semverInfo("Enabling extension configured strategy")
                initialConfig.withBranchMatchingConfig(versionStrategy.get())
            }

            git.hasBranch(GitRef.Branch.DEVELOP.name).isNotEmpty() -> {
                log.semverInfo("Enabling Git Flow mode")
                initialConfig.withBranchMatchingConfig(flowVersionCalculatorStrategy(versionModifier.get()))
            }

            else -> {
                log.semverInfo("Enabling Flat mode")
                initialConfig.withBranchMatchingConfig(flatVersionCalculatorStrategy(versionModifier.get()))
            }
        }
    }
}
