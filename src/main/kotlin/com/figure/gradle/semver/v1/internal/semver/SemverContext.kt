/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1.internal.semver

import com.figure.gradle.semver.v1.internal.git.GitRef
import com.figure.gradle.semver.v1.internal.semverWarn
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

interface SemverContext {
    val ops: ContextProviderOperations

    fun property(name: String): Any?
    fun env(name: String): String?

    fun preReleaseWithCommitCount(
        currentBranch: GitRef.Branch,
        targetBranch: GitRef.Branch,
        label: String
    ) = PreReleaseLabel(
        value = ops.commitsSinceBranchPoint(currentBranch, targetBranch).fold(
            onSuccess = {
                "$label.$it"
            },
            onFailure = {
                log.semverWarn("Unable to calculate commits since branch point on current $currentBranch")
                label
            }
        )
    )
}

class GradleSemverContext(
    private val project: Project,
    override val ops: ContextProviderOperations,
) : SemverContext {
    override fun property(name: String): Any? =
        project.findProperty(name)

    override fun env(name: String): String? =
        System.getenv(name)
}
