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

import com.figure.gradle.semver.internal.command.GitState
import com.figure.gradle.semver.internal.command.KGit
import com.figure.gradle.semver.internal.errors.InvalidOverrideVersionError
import com.figure.gradle.semver.internal.logging.warn
import com.figure.gradle.semver.internal.properties.Modifier
import com.figure.gradle.semver.internal.properties.Stage
import io.github.z4kn4fein.semver.nextPatch
import io.github.z4kn4fein.semver.toVersion
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

fun ProviderFactory.versionFactory(
    context: VersionFactoryContext,
): Provider<String> =
    of(VersionFactory::class.java) { spec ->
        spec.parameters {
            it.versionFactoryContext.set(context)
        }
    }

abstract class VersionFactory : ValueSource<String, VersionFactory.Params> {
    interface Params : ValueSourceParameters {
        val versionFactoryContext: Property<VersionFactoryContext>
    }

    private fun Params.toVersionCalculatorContext(gitState: GitState) =
        with(versionFactoryContext.get()) {
            VersionCalculatorContext(
                stage = stage,
                modifier = modifier,
                forTesting = forTesting,
                gitState = gitState,
                mainBranch = mainBranch,
                developmentBranch = developmentBranch,
                appendBuildMetadata = appendBuildMetadata,
            )
        }

    override fun obtain(): String {
        val factoryContext = parameters.versionFactoryContext.get()

        if (!factoryContext.rootDir.resolve(".git").exists()) {
            log.warn { "Git is not initialized in this repository. Please run 'git init' to initialize it." }
            log.warn { "Alternatively, for composite projects, specify the `rootProjectDir` in the semver configuration block." }
            val nextVersion = factoryContext.initialVersion.toVersion().nextPatch().toString()
            return "$nextVersion-UNINITIALIZED-REPO"
        }

        if (factoryContext.modifier == Modifier.Major && factoryContext.forMajorVersion != null) {
            error("forMajorVersion cannot be used with the 'major' modifier")
        }

        val kgit = KGit(directory = factoryContext.rootDir)

        val context = parameters.toVersionCalculatorContext(kgit.state())

        val overrideVersion = factoryContext.overrideVersion
        val latestVersion = kgit.tags.latestOrInitial(factoryContext.initialVersion, factoryContext.forMajorVersion)
        val latestNonPreReleaseVersion = kgit.tags.latestNonPreReleaseOrInitial(factoryContext.initialVersion)

        val version = when {
            context.gitState != GitState.NOMINAL -> {
                GitStateVersionCalculator.calculate(latestNonPreReleaseVersion, context)
            }

            overrideVersion != null -> {
                runCatching {
                    overrideVersion.toVersion()
                }.getOrElse {
                    throw InvalidOverrideVersionError(overrideVersion)
                }.toString()
            }

            kgit.branch.isOnMainBranch(context.mainBranch, context.forTesting) -> {
                StageVersionCalculator.calculate(latestVersion, context)
            }

            // Works for any branch
            else -> {
                // Compute based on the branch name, otherwise, use the stage to compute the next version
                if (context.stage == Stage.Auto) {
                    BranchVersionCalculator(kgit).calculate(latestNonPreReleaseVersion, context)
                } else {
                    StageVersionCalculator.calculate(latestVersion, context)
                }
            }
        }

        return version
    }
}
