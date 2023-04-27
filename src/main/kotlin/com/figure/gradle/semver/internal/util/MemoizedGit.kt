/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.internal.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

object MemoizedGit {
    private val memoizedGit = mutableMapOf<String, Git>()

    fun open(gitDir: String): Git =
        memoizedGit.computeIfAbsent(gitDir) {
            Git(
                FileRepositoryBuilder()
                    .setGitDir(File(gitDir))
                    .readEnvironment()
                    .findGitDir()
                    .build()
            )
        }
}
