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

import com.figure.gradle.semver.internal.command.extension.shortName
import com.figure.gradle.semver.internal.environment.Env
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME
import org.eclipse.jgit.lib.Constants.R_REMOTES
import org.eclipse.jgit.lib.Ref

class Branch(
    private val git: Git,
    private val branchList: BranchList,
) {
    val headRef: Ref
        get() = git.repository.exactRef(Constants.HEAD)

    val currentRef: Ref
        get() {
            val refName = when {
                Env.isCI -> Env.githubHeadRef ?: Env.githubRefName
                else -> git.repository.branch
            }
            return branchList.find(refName) ?: error("Could not find current branch: $refName")
        }

    fun isOnMainBranch(providedMainBranch: String? = null): Boolean =
        currentRef.shortName == branchList.findMainBranch(providedMainBranch).shortName

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
