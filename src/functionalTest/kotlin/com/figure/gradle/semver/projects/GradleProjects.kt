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

import com.figure.gradle.semver.git.GitInstance
import com.figure.gradle.semver.gradle.build
import com.figure.gradle.semver.gradle.buildAndFail
import com.figure.gradle.semver.gradle.runWithoutExpectations
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion

class GradleProjects(
    private val projects: Map<String, AbstractProject>,
) : AutoCloseable {
    companion object {
        fun gradleProjects(vararg projects: AbstractProject) =
            GradleProjects(projects.associateBy { it.projectName })
    }

    val versions: List<String>
        get() = projects.values.map { it.version }

    val versionTags: List<String>
        get() = projects.values.map { it.versionTag }

    fun git(gitInstance: GitInstance) {
        projects.values.forEach { it.git(gitInstance) }
    }

    fun git(block: GitInstance.Builder.() -> Unit) {
        projects.values.forEach { it.git(block) }
    }

    fun cleanAfterAny() {
        projects.values.forEach { it.cleanAfterAny() }
    }

    fun build(gradleVersion: GradleVersion, vararg args: String): Map<AbstractProject, BuildResult> =
        projects.values.associateWith { project ->
            build(gradleVersion, project.gradleProject.rootDir, *args)
        }

    // TODO: Anywhere this is used, check for the specific message, not just BUILD FAILED
    fun runWithoutExpectations(gradleVersion: GradleVersion, vararg args: String): Map<AbstractProject, BuildResult> =
        projects.values.associateWith { project ->
            runWithoutExpectations(gradleVersion, project.gradleProject.rootDir, *args)
        }

    fun buildAndFail(gradleVersion: GradleVersion, vararg args: String): Map<AbstractProject, BuildResult> =
        projects.values.associateWith { project ->
            buildAndFail(gradleVersion, project.gradleProject.rootDir, *args)
        }

    override fun close() {
        projects.values.forEach { it.close() }
    }
}
