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

import com.figure.gradle.semver.internal.extensions.isNotPreRelease
import com.figure.gradle.semver.internal.properties.Stage
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.toVersionOrNull
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref

class TagList(
    private val git: Git,
) {
    operator fun invoke(): List<Ref> =
        git.tagList().call()

    fun find(tagName: String): Ref? =
        invoke().find { it.name == tagName }

    val versionedTags: List<Version>
        get() = invoke().mapNotNull { it.name.replace(Constants.R_TAGS, "").toVersionOrNull(strict = false) }

    private fun latest(forMajorVersion: Int?): Version? {
        val stages = Stage.entries.map { stage -> stage.value.lowercase() }

        return versionedTags
            // Get only stable and staged pre-releases
            .filter { version ->
                val prereleaseLabel = version.preRelease?.substringBefore(".")?.lowercase()
                version.isNotPreRelease || prereleaseLabel in stages
            }
            .let { versions ->
                if (forMajorVersion != null) {
                    versions.filter { version -> version.major == forMajorVersion }
                } else {
                    versions
                }
            }
            .maxOrNull()
    }

    fun latestOrInitial(initial: String, forMajorVersion: Int?): Version =
        latest(forMajorVersion) ?: initial.toVersion()

    private val latestNonPreRelease: Version?
        get() = versionedTags
            .filter { version -> version.isNotPreRelease }
            .maxOrNull()

    fun latestNonPreReleaseOrInitial(initial: String): Version =
        latestNonPreRelease ?: initial.toVersion()
}
