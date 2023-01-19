/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1.internal.exceptions

import com.figure.gradle.semver.v1.internal.git.GitRef

class GitException(t: Throwable) :
    Exception(t)

class UnexpectedException(message: String) :
    Exception("Something unexpected occurred: $message")

class MissingBranchMatchingConfigurationException(currentBranch: GitRef.Branch) :
    Exception("Missing branch matching configuration for current branch: ${currentBranch.name}")
