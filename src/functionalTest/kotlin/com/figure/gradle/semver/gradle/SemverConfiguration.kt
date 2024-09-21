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
package com.figure.gradle.semver.gradle

import com.figure.gradle.semver.kit.render.Element
import com.figure.gradle.semver.kit.render.Scribe
import java.io.File

fun semver(fn: SemverConfiguration.Builder.() -> Unit): SemverConfiguration {
    val builder = SemverConfiguration.Builder()
    builder.fn()
    return builder.build()
}

class SemverConfiguration(
    val rootProjectDir: File? = null,
    val initialVersion: String?,
    val mainBranch: String?,
    val developmentBranch: String?,
    val appendBuildMetadata: String?,
) : Element.Block {
    override val name: String = "semver"

    override fun render(scribe: Scribe): String =
        scribe.block(this) { s ->
            rootProjectDir?.let {
                s.line { s.append("rootProjectDir = \"$rootProjectDir\"") }
            }
            initialVersion?.let {
                s.line { s.append("initialVersion = \"$initialVersion\"") }
            }
            mainBranch?.let {
                s.line { s.append("mainBranch = \"$mainBranch\"") }
            }
            developmentBranch?.let {
                s.line { s.append("developmentBranch = \"$developmentBranch\"") }
            }
            appendBuildMetadata?.let {
                s.line { s.append("appendBuildMetadata = \"$appendBuildMetadata\"") }
            }
        }

    class Builder {
        var rootProjectDir: File? = null
        var initialVersion: String? = null
        var mainBranch: String? = null
        var developmentBranch: String? = null
        var appendBuildMetadata: String? = null

        fun build(): SemverConfiguration =
            SemverConfiguration(
                rootProjectDir = rootProjectDir,
                initialVersion = initialVersion,
                mainBranch = mainBranch,
                developmentBranch = developmentBranch,
                appendBuildMetadata = appendBuildMetadata,
            )
    }
}
