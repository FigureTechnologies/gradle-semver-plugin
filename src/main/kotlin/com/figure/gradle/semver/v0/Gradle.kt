/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v0

import org.gradle.api.Project
import org.gradle.api.logging.Logger

internal val Project.hasSemverPlugin: Boolean
    get() = pluginManager.hasPlugin("com.figure.gradle.semver-plugin")

internal val Project.appliedOnlyOnRootProject: Boolean
    get() = rootProject.hasSemverPlugin && rootProject.subprojects.none(Project::hasSemverPlugin)

class GradleSemverContext(private val project: Project, override val ops: ContextProviderOperations): SemverContext {
    override fun property(name: String): Any? {
        return project.findProperty(name)
    }

    override fun env(name: String): String? {
        return System.getenv(name)
    }
}

private val LogPrefix = "semver: ".bold()

private fun String.coloured(c: String) = "$c$this\u001B[0m"
internal fun String.green() = this.coloured("\u001B[32m")
internal fun String.red() = this.coloured("\u001B[31m")
internal fun String.purple() = this.coloured("\u001B[35m")
internal fun String.yellow() = this.coloured("\u001B[33m")
internal fun String.bold() = this.coloured("\u001B[1m")

internal fun Logger.semver(message: String) = this.lifecycle("semver: ".bold() + message.purple())
internal fun Logger.semverInfo(message: String) = this.info("semver: ".bold() + message.purple())
internal fun Logger.semverWarn(message: String) = this.lifecycle("semver: ".bold() + message.yellow())
internal fun Logger.semverError(message: String) = this.lifecycle("semver: ".bold() + message.red())
