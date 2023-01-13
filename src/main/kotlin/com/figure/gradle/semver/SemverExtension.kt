/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver

import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import com.figure.gradle.semver.domain.GitRef
import com.figure.gradle.semver.domain.toError
import net.swiftzer.semver.SemVer
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

private val logger = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

abstract class SemverExtension @Inject constructor(objects: ObjectFactory, private val project: Project) {
    val gitLocation: Property<String> = objects.property(String::class.java).convention(null)
    private val tagPrefix: Property<String> = objects.property(String::class.java).convention(VersionCalculatorConfig.DefaultTagPrefix)
    private val initialVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(VersionCalculatorConfig.DefaultVersion)
    private val overrideVersion: Property<SemVer> = objects.property(SemVer::class.java).convention(null)
    private val versionStrategy: ListProperty<BranchMatchingConfiguration> = objects.listProperty(BranchMatchingConfiguration::class.java).convention(null)

    // TODO?
    private var versionModifier: VersionModifier = { nextPatch() }

    fun tagPrefix(prefix: String) {
        if (overrideVersion.orNull != null)
            throw IllegalArgumentException("Cannot set the semver tagPrefix after override version has been set, the override version depends on the tagPrefix, set the tagPrefix first")
        tagPrefix.set(prefix)
    }

    fun initialVersion(version: String?) {
        version?.also {
            initialVersion.set(SemVer.parse(it))
        }
    }

    fun overrideVersion(version: String) {
        overrideVersion.set(possiblyPrefixedVersion(version, tagPrefix.get())) // not great, requires tagPrefix to be set first
    }

    fun versionModifier(modifier: VersionModifier) {
        this.versionModifier = modifier
    }
    fun buildVersionModifier(modifier: String): VersionModifier {
        return versionModifierFromString(modifier).fold({
            logger.error("unknown version modifier [$modifier]")
            throw Exception("unknown version modifier [$modifier]")
        }, {
            it
        })
    }
    fun versionCalculatorStrategy(strategy: VersionCalculatorStrategy) {
        versionStrategy.set(strategy)
    }

    // defer version calculation since all our properties are lazy and need to be configured first
    private fun version(): SemVer {
        logger.semver("Git location: ${gitLocation.orNull}")

        val git = project.git(gitLocation.orNull)
        val config = buildCalculatorConfig(git)
        val ops = getGitContextProviderOperations(git, config)
        val context = GradleSemverContext(project, ops)

        return config.overrideVersion.getOrElse {
            ops.currentBranch().fold({
                logger.semverError("failed to find current branch, cannot calculate semver")
                throw Exception("failed to find current branch")
            }, { currentBranch ->
                logger.semver("current branch: $currentBranch")
                val calculator = getTargetBranchVersionCalculator(ops, config, context, currentBranch)

                // log for debugging, don't want this as a lifecycle log
                // logger.semver("semver configuration while calculating version: $config")

                calculator.calculateVersion().getOrHandle {
                    logger.semverError("failed to calculate version: ${it.toError()}")
                    throw Exception("$it")
                }
            })
        }.also {
            logger.semver(it.toString())
        }
    }

    private fun versionTagName(): String = tagPrefix.map { "$it${version}" }.get()

    private fun possiblyPrefixedVersion(version: String, prefix: String): SemVer {
        return SemVer.parse(version.trimMargin(prefix)) // fail fast, don't let an invalid version propagate to runtime
    }

    val version by lazy { version().toString() }
    val versionTagName by lazy { versionTagName() }

    private fun buildCalculatorConfig(git: Git): VersionCalculatorConfig {
        val initialConfig = VersionCalculatorConfig(
            tagPrefix.get(),
            initialVersion.get(),
            overrideVersion.orNull.toOption(),
        )
        return when {
            versionStrategy.isPresent -> {
                logger.semver("enabling extension configured strategy")
                initialConfig.withBranchMatchingConfig(versionStrategy.get())
            }
            git.hasBranch(GitRef.Branch.Develop.name).isNotEmpty() -> {
                logger.semver("enabling Git Flow mode")
                initialConfig.withBranchMatchingConfig(FlowVersionCalculatorStrategy(versionModifier))
            }
            else -> { // if we don't have a develop branch, fallback to Flat mode
                logger.semver("enabling Flat mode")
                initialConfig.withBranchMatchingConfig(FlatVersionCalculatorStrategy(versionModifier))
            }
        }
    }

    companion object {
        const val ExtensionName = "semver"

        internal fun Project.semver(): SemverExtension = extensions.create(ExtensionName, SemverExtension::class.java)
    }
}
