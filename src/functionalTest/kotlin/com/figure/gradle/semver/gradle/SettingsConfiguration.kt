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

fun settingsGradle(
    fn: SettingsConfiguration.Builder.() -> Unit,
): SettingsConfiguration {
    val builder = SettingsConfiguration.Builder()
    builder.fn()
    return builder.build()
}

class SettingsConfiguration(
    private var buildCache: BuildCache? = null,
    private var semver: SemverConfiguration? = null,
) : Element.Line {
    override fun render(scribe: Scribe): String = buildString {
        semver?.let { sv ->
            append(scribe.use { s -> sv.render(s) })
            appendLine()
        }

        buildCache?.let { bc ->
            append(scribe.use { s -> bc.render(s) })
            appendLine()
        }
    }

    class Builder {
        var buildCache: BuildCache? = null
        var semver: SemverConfiguration? = null

        fun build(): SettingsConfiguration =
            SettingsConfiguration(
                buildCache = buildCache,
                semver = semver,
            )
    }
}
