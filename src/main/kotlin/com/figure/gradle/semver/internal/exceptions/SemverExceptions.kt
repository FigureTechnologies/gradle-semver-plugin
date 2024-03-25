/**
 * Copyright (c) 2024 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.exceptions

import com.figure.gradle.semver.internal.git.GitRef

internal class GitException(t: Throwable) :
    Exception(t)

internal class UnexpectedException(message: String) :
    Exception("Something unexpected occurred: $message")

internal class MissingBranchMatchingConfigurationException(currentBranch: GitRef.Branch) :
    Exception("Missing branch matching configuration for current branch: ${currentBranch.name}")

internal class TagAlreadyExistsException(tag: String) :
    Exception(
        """
        |Tag $tag already exists on remote! Either skip publishing the artifact on the next run or delete
        | the existing tag before running again.
        """.trimMargin().replace("\n", ""),
    )

internal class UnsupportedBranchingStrategy() :
    Exception(
        "Unsupported branching strategy. Supported branching strategies: main, master, main-develop, master-develop",
    )
