/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1

import com.figure.gradle.semver.v1.internal.git.GitRef
import com.figure.gradle.semver.v1.internal.git.git
import com.figure.gradle.semver.v1.internal.git.hasBranch
import com.figure.gradle.semver.v1.internal.property
import com.figure.gradle.semver.v1.internal.semver.BranchMatchingConfiguration
import com.figure.gradle.semver.v1.internal.semver.GitContextProviderOperations
import com.figure.gradle.semver.v1.internal.semver.GradleSemverContext
import com.figure.gradle.semver.v1.internal.semver.TargetBranchVersionCalculator
import com.figure.gradle.semver.v1.internal.semver.VersionCalculatorConfig
import com.figure.gradle.semver.v1.internal.semver.flatVersionCalculatorStrategy
import com.figure.gradle.semver.v1.internal.semver.flowVersionCalculatorStrategy
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

open class SemverExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    val tempVar: Property<String> = objects.property {
        println("Setting a default")
        set("This is your default")
    }

    val gitDir: Property<String> =
        objects.property<String>()
            .convention("${project.rootProject.rootDir.path}/.git")

    val tagPrefix: Property<String> =
        objects.property<String>()
            .convention(VersionCalculatorConfig.DEFAULT_TAG_PREFIX)

    val initialVersion: Property<SemVer> =
        objects.property<SemVer>()
            .convention(VersionCalculatorConfig.DEFAULT_VERSION)

    val versionStrategy: ListProperty<BranchMatchingConfiguration> =
        objects.listProperty<BranchMatchingConfiguration>()
            .convention(null)

    val overrideVersion: Property<SemVer> =
        objects.property<SemVer>()
            .convention(null)

    /**
     * This version invocation takes place at project build time of the project that is utilizing the plugin
     * The version is not calculated until a build happens that requires `semver.version`
     */
    val version by lazy { calculateVersion().toString() }

    val versionTagName by lazy { calculateVersionTagName() }

    private fun calculateVersion(): SemVer {
        log.info("Using git directory: ${gitDir.get()}")

        val git = project.git(gitDir.get())
        val config = buildCalculatorConfig(git)
        val ops = GitContextProviderOperations(git, config)
        val context = GradleSemverContext(project, ops)

        return config.overrideVersion ?: run {
            ops.currentBranch()?.let { currentBranch ->
                log.info("Current branch: $currentBranch")
                log.info("Semver configuration while calculating version: $config")

                val calculator = TargetBranchVersionCalculator(ops, config, context, currentBranch)
                calculator.calculateVersion().getOrElse {
                    log.error("Failed to calculate version", it)
                    throw Exception(it)
                }
            } ?: run {
                log.error("Failed to find current branch, cannot calculate semver")
                throw Exception("Failed to find current branch")
            }
        }.also {
            log.info("Using semver: $it")
        }
    }

    private fun calculateVersionTagName(): String {
        return tagPrefix.map { prefix -> "$prefix$version" }.get()
    }

    private fun buildCalculatorConfig(git: Git): VersionCalculatorConfig {
        val initialConfig = VersionCalculatorConfig(
            tagPrefix = tagPrefix.get(),
            initialVersion = initialVersion.get(),
            overrideVersion.orNull
        )
        return when {
            versionStrategy.isPresent -> {
                log.info("Enabling extension configured strategy")
                initialConfig.withBranchMatchingConfig(versionStrategy.get())
            }

            git.hasBranch(GitRef.Branch.DEVELOP.name).isNotEmpty() -> {
                log.info("Enabling Git Flow mode")
                initialConfig.withBranchMatchingConfig(flowVersionCalculatorStrategy { nextPatch() })
            }

            else -> {
                log.info("Enabling Flat mode")
                initialConfig.withBranchMatchingConfig(flatVersionCalculatorStrategy { nextPatch() })
            }
        }
    }
}
