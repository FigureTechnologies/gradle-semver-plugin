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
package com.figure.gradle.semver.projects

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.figure.gradle.semver.Constants
import com.figure.gradle.semver.git.GitInstance
import com.figure.gradle.semver.git.GitInstanceWriter
import com.figure.gradle.semver.internal.command.InitializeRepo
import com.figure.gradle.semver.internal.command.KGit
import com.figure.gradle.semver.kit.render.Scribe
import java.io.File
import java.util.Properties
import kotlin.io.path.createTempDirectory

abstract class AbstractProject : AbstractGradleProject(), AutoCloseable {
    abstract val gradleProject: GradleProject
    abstract val projectName: String

    private lateinit var remoteRepoDir: File

    val dslKind: GradleProject.DslKind = GradleProject.DslKind.KOTLIN

    val scribe = Scribe(
        dslKind = dslKind,
        indent = 2,
    )

    val buildCacheDir: File =
        createTempDirectory("build-cache").toFile()

    val version: String
        get() = fetchSemverProperties().getProperty("version")

    val versionTag: String
        get() = fetchSemverProperties().getProperty("versionTag")

    fun git(block: GitInstance.Builder.() -> Unit) {
        val builder = GitInstance.Builder(this)
        builder.block()
        git(builder.build())
    }

    fun git(gitInstance: GitInstance) {
        remoteRepoDir = createTempDirectory("remote-repo").toFile()

        val localGit = KGit(gradleProject.rootDir, initializeRepo = InitializeRepo(bare = false, gitInstance.initialBranch))
        val remoteGit = KGit(remoteRepoDir, initializeRepo = InitializeRepo(bare = true, gitInstance.initialBranch))

        // GHA needs this since no author is configured in the runner
        localGit.config.author("Al Gorithm", "al.gori@thm.com")

        localGit.remote.add(remoteGit.git)

        localGit.commit("Initial commit", allowEmptyCommit = true)

        val gitInstanceWriter = GitInstanceWriter(
            localGit = localGit,
            gitActionsConfig = gitInstance.actions,
        )

        gitInstanceWriter.write(gitInstance.debugging)
    }

    fun cleanAfterAny() {
        // The remote repo must be recreated for each test
        if (this::remoteRepoDir.isInitialized) {
            remoteRepoDir.deleteRecursively()
        }
        gradleProject.rootDir.resolve(".git").deleteRecursively()
    }

    override fun close() {
        buildCacheDir.deleteRecursively()
    }

    protected open fun fetchSemverProperties(): Properties =
        gradleProject.rootDir.resolve(Constants.SEMVER_PROPERTY_PATH)
            .let { Properties().apply { load(it.reader()) } }
}
