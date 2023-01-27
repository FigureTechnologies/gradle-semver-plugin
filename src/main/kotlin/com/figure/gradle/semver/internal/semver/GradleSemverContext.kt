/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.semver

import com.figure.gradle.semver.external.ContextProviderOperations
import com.figure.gradle.semver.external.SemverContext
import org.gradle.api.Project

internal class GradleSemverContext(
    private val project: Project,
    override val ops: ContextProviderOperations,
) : SemverContext {
    override fun property(name: String): Any? =
        project.findProperty(name)

    override fun env(name: String): String? =
        System.getenv(name)
}
