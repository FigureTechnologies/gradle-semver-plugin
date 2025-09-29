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
import org.eclipse.jgit.lib.ObjectId
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
            val foundRef = branchList.find(refName)
            if (foundRef != null) {
                return foundRef
            }

            // Handle cross-repo PRs where the branch doesn't exist locally
            // This happens when GitHub Actions checks out the target repository but the
            // branch name from GITHUB_HEAD_REF (from forked repo) doesn't exist locally
            return if (Env.isCI && Env.githubHeadRef != null) {
                // Create a synthetic ref object that preserves the original branch name
                // This ensures version calculation uses the actual feature branch name
                // instead of falling back to HEAD or the merge ref
                SyntheticRef(name = "refs/heads/${Env.githubHeadRef}", target = headRef.objectId)
            } else {
                // For non-CI environments or when not in a PR, use HEAD
                headRef
            }
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

/**
 * Synthetic Ref implementation for cross-repository PRs where the branch doesn't exist locally
 * but we want to preserve the original branch name for version calculation
 */
private class SyntheticRef(
    private val name: String,
    private val target: ObjectId,
) : Ref {
    override fun getName(): String = name

    override fun isSymbolic(): Boolean = false

    override fun getLeaf(): Ref = this

    override fun getTarget(): Ref? = null

    override fun getObjectId(): ObjectId = target

    override fun isPeeled(): Boolean = true

    override fun getPeeledObjectId(): ObjectId? = target

    override fun getStorage(): Ref.Storage = Ref.Storage.LOOSE
}
