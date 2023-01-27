/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import com.figure.gradle.semver.external.BranchMatchingConfiguration
import com.figure.gradle.semver.external.VersionCalculatorStrategy
import com.figure.gradle.semver.external.VersionModifier
import com.figure.gradle.semver.external.flatVersionCalculatorStrategy
import com.figure.gradle.semver.external.flowVersionCalculatorStrategy
import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.internal.git.git
import com.figure.gradle.semver.internal.git.hasBranch
import com.figure.gradle.semver.internal.semver.GitContextProviderOperations
import com.figure.gradle.semver.internal.semver.GradleSemverContext
import com.figure.gradle.semver.internal.semver.TargetBranchVersionCalculator
import com.figure.gradle.semver.internal.semver.VersionCalculatorConfig
import com.figure.gradle.semver.internal.semver.versionModifierFromString
import com.figure.gradle.semver.internal.semverError
import com.figure.gradle.semver.internal.semverInfo
import com.figure.gradle.semver.internal.semverLifecycle
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

abstract class SemverExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    /**
     * This version invocation takes place at project build time of the project that is utilizing this plugin
     * The version is not calculated until a build happens that requires `semver.version`
     */
    val version: String by lazy { calculateVersion().toString() }

    val versionTagName: String by lazy { calculateVersionTagName() }

    internal val gitDir: Property<String> =
        objects.property<String>()
            .convention("${project.rootProject.rootDir.path}/.git")

    private val tagPrefix: Property<String> =
        objects.property<String>()
            .convention(VersionCalculatorConfig.DEFAULT_TAG_PREFIX)

    private val initialVersion: Property<SemVer> =
        objects.property<SemVer>()
            .convention(VersionCalculatorConfig.DEFAULT_VERSION)

    private val versionStrategy: ListProperty<BranchMatchingConfiguration> =
        objects.listProperty<BranchMatchingConfiguration>()
            .convention(null)

    private val overrideVersion: Property<SemVer> =
        objects.property<SemVer>()
            .convention(null)

    private val versionModifier: Property<VersionModifier> =
        objects.property<VersionModifier>()
            .convention { nextPatch() }

    fun gitDir(gitDir: String) {
        this.gitDir.set(gitDir)
    }

    fun tagPrefix(prefix: String) {
        if (overrideVersion.orNull != null) {
            throw IllegalArgumentException(
                "Cannot set semver tagPrefix after override version has been set. " +
                    "The override version depends on the tagPrefix. Set the tagPrefix first."
            )
        }
        this.tagPrefix.set(prefix)
    }

    fun initialVersion(version: String?) {
        version?.also {
            this.initialVersion.set(SemVer.parse(it))
        }
    }

    fun overrideVersion(version: String) {
        this.overrideVersion.set(possiblyPrefixVersion(version, tagPrefix.get()))
    }

    fun versionModifier(modifier: VersionModifier) {
        this.versionModifier.set(modifier)
    }

    fun buildVersionModifier(modifier: String): VersionModifier {
        return versionModifierFromString(modifier)
    }

    fun versionCalculatorStrategy(strategy: VersionCalculatorStrategy) {
        this.versionStrategy.set(strategy)
    }

    private fun calculateVersion(): SemVer {
        log.semverInfo("Using git directory: ${gitDir.get()}")

        val git = project.git(gitDir.get())
        val config = buildCalculatorConfig(git)
        val ops = GitContextProviderOperations(git, config)
        val context = GradleSemverContext(project, ops)

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

    private fun calculateVersionTagName(): String {
        return tagPrefix.map { prefix -> "$prefix$version" }.get()
    }

    private fun buildCalculatorConfig(git: Git): VersionCalculatorConfig {
        val initialConfig = VersionCalculatorConfig(
            tagPrefix = tagPrefix.get(),
            initialVersion = initialVersion.get(),
            overrideVersion = overrideVersion.orNull
        )
        return when {
            versionStrategy.isPresent -> {
                log.semverInfo("Enabling extension configured strategy")
                initialConfig.withBranchMatchingConfig(versionStrategy.get())
            }

            git.hasBranch(GitRef.Branch.DEVELOP.name).isNotEmpty() -> {
                log.semverInfo("Enabling Git Flow mode")
                initialConfig.withBranchMatchingConfig(flowVersionCalculatorStrategy { nextPatch() })
            }

            else -> {
                log.semverInfo("Enabling Flat mode")
                initialConfig.withBranchMatchingConfig(flatVersionCalculatorStrategy { nextPatch() })
            }
        }
    }

    private fun possiblyPrefixVersion(version: String, prefix: String): SemVer {
        return SemVer.parse(version.trimMargin(prefix)) // fail fast, don't let an invalid version propagate to runtime
    }
}
