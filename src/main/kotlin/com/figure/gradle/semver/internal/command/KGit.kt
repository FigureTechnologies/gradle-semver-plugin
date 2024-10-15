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
package com.figure.gradle.semver.internal.command

import org.eclipse.jgit.api.Git
import java.io.File

class KGit(
    directory: File? = null,
    initializeRepo: InitializeRepo? = null,
) : AutoCloseable {
    val git: Git by lazy {
        when {
            directory != null && initializeRepo != null -> init(
                directory,
                bare = initializeRepo.bare,
                initialBranch = initializeRepo.initialBranch,
            )

            directory != null -> open(directory)
            else -> open()
        }
    }

    companion object {
        val init = Init
        val open = Open
    }

    val add = Add(git)
    val checkout = Checkout(git)
    val config = Config(git)
    val branches = BranchList(git)
    val branch = Branch(git, branches)
    val commit = Commit(git)
    val log = Log(git)
    val print = Print(this)
    val push = Push(git)
    val remote = Remote(git)
    val state = State(git)
    val tag = Tag(git)
    val tags = TagList(git)

    override fun close() = git.close()
}

data class InitializeRepo(
    val bare: Boolean,
    val initialBranch: String,
)
