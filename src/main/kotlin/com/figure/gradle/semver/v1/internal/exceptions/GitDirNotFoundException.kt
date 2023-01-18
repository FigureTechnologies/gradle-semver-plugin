/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1.internal.exceptions

class GitDirNotFoundException(gitDir: String) : Exception(
    """
        |The directory $gitDir does not exist.
        | If this should be the location of your git directory, please initialize a repo.
        | Alternatively, specify the location of your git directory via `gitDir.set(..)` in the `semver` extension 
        | if, for example, this project is a composite build.
    """.trimMargin().replace("\n", "")
)
