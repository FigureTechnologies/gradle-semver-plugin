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
import com.figure.gradle.semver.internal.semver.VersionCalculatorConfig
import com.figure.gradle.semver.internal.semver.versionModifierFromString
import com.figure.gradle.semver.internal.valuesources.gitCalculateSemverProvider
import net.swiftzer.semver.SemVer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class SemverExtension @Inject constructor(
    objects: ObjectFactory,
    project: Project,
    providerFactory: ProviderFactory,
) {
    /**
     * This version invocation takes place at project build time of the project that is utilizing this plugin
     * The version is not calculated until a build happens that requires `semver.version`
     */
    val version: String by lazy {
        providerFactory.gitCalculateSemverProvider(
            gitDir = gitDir,
            tagPrefix = tagPrefix,
            initialVersion = initialVersion.orNull.toString(),
            overrideVersion = overrideVersion.orNull.toString(),
            versionStrategy = versionStrategy,
            versionModifier = versionModifier,
        ).get()
    }

    val versionTagName: String by lazy { calculateVersionTagName() }

    // TODO: For v2, see if rootRepoDirectory can be specified instead (without causing Gradle issues) where
    //  a semver task relies on the build task to complete. Maybe needs specified as a string instead of File to
    //  work? Use Git.open(rootRepoDirectory) to create the Git object then.
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
                """
                |Cannot set semver tagPrefix after override version has been set. 
                |The override version depends on the tagPrefix. Set the tagPrefix first.
                """.trimMargin()
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

    private fun calculateVersionTagName(): String {
        return tagPrefix.map { prefix -> "$prefix$version" }.get()
    }

    private fun possiblyPrefixVersion(version: String, prefix: String): SemVer {
        return SemVer.parse(version.trimMargin(prefix)) // fail fast, don't let an invalid version propagate to runtime
    }
}
