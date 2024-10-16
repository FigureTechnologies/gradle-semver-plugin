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

import com.figure.gradle.semver.internal.command.extension.revWalk
import com.figure.gradle.semver.internal.extensions.R_REMOTES_ORIGIN
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref

class BranchList(
    private val git: Git,
) {
    fun findDevelopmentBranch(providedDevelopmentBranch: String?, providedMainBranch: String?): Ref =
        find(providedDevelopmentBranch)
            ?: find("develop")
            ?: find("devel")
            ?: find("dev")
            ?: find(providedMainBranch) // Need to fall back in cases where the main branch is not main or master
            ?: find("main")
            ?: find("master")
            ?: error(
                buildString {
                    append("Could not determine default branch. ")
                    append("Searched, in order, for: ")
                    append("$providedDevelopmentBranch, develop, devel, dev, $providedMainBranch, main, master")
                },
            )

    fun findMainBranch(providedMainBranch: String?): Ref =
        find(providedMainBranch)
            ?: find("main")
            ?: find("master")
            ?: error("Could not determine main branch. Searched, in order, for: $providedMainBranch, main, master")

    fun exists(branchName: String): Boolean =
        find(branchName) != null

    /**
     * Finds an exact branch by name preferring local branches over remote branches, but will return
     * remote branches if the local branch does not exist.
     */
    fun find(branchName: String?): Ref? =
        branchName
            ?.takeIf { it.isNotBlank() }
            ?.let { nonBlankBranchName ->
                findAll(nonBlankBranchName).let { matchingBranches ->
                    matchingBranches.find { Constants.R_HEADS in it.name }
                        ?: matchingBranches.find { Constants.R_REMOTES in it.name }
                }
            }

    /**
     * Find all branches given the branch name. Can be full or short name.
     */
    private fun findAll(branchName: String): List<Ref> =
        git.branchList()
            .setListMode(ListBranchCommand.ListMode.ALL)
            .call()
            .filter { branchName.lowercase() in it.name.lowercase() }

    fun commitCountBetween(baseBranchName: String, targetBranchName: String): Int {
        // Try to resolve the remote branch first, then fall back to the local branch
        // This should fix situations where you're on the base branch with commits locally.
        // If that's the case, you'll likely get 0 commits between the base and the target branch.
        val baseBranch: ObjectId = git.repository.resolve("$R_REMOTES_ORIGIN/$baseBranchName")
            ?: git.repository.resolve(baseBranchName)

        val targetBranch: ObjectId = git.repository.resolve(targetBranchName)

        return git.revWalk { revWalk ->
            revWalk.apply {
                markStart(parseCommit(targetBranch))
                markUninteresting(parseCommit(baseBranch))
            }

            revWalk.toList().size
        }
    }
}
