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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

class Branch(
    private val git: Git,
    private val branchList: BranchList,
) {
    private val githubHeadRef: String = "GITHUB_HEAD_REF"

    private val shortName: String
        get() = git.repository.branch

    val fullName: String
        get() = git.repository.fullBranch

    private val branchRef: Ref
        get() = git.repository.findRef(shortName)

    val headRef: Ref?
        get() = git.repository.exactRef(Constants.HEAD)

    val headCommit: RevCommit
        get() = git.revWalk { it.parseCommit(branchRef.objectId) }

    fun currentRef(forTesting: Boolean = false): Ref =
        if (forTesting) {
            branchRef
        } else {
            git.repository.findRef(System.getenv(githubHeadRef) ?: shortName)
        }

    fun isOnMainBranch(providedMainBranch: String? = null, forTesting: Boolean = false): Boolean =
        currentRef(forTesting).name == branchList.findMainBranch(providedMainBranch).name

    fun create(branchName: String): Ref =
        git.branchCreate()
            .setName(branchName)
            .call()

    fun delete(vararg branchNames: String): List<String> =
        git.branchDelete()
            .setBranchNames(*branchNames)
            .setForce(true)
            .call()
}
