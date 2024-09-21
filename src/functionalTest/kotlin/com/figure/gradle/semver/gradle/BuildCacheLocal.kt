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

fun local(fn: BuildCacheLocal.Builder.() -> Unit): BuildCacheLocal {
    val builder = BuildCacheLocal.Builder()
    builder.fn()
    return builder.build()
}

class BuildCacheLocal(
    private val directory: File? = null,
) : Element.Block {
    override val name: String = "local"

    override fun render(scribe: Scribe): String =
        scribe.block(this) { s ->
            directory?.let {
                s.line {
                    s.append("directory = file(\"")
                    s.append(directory.absolutePath)
                    s.append("\")")
                }
            }
        }

    class Builder {
        var directory: File? = null

        fun build(): BuildCacheLocal =
            BuildCacheLocal(
                directory = directory,
            )
    }
}
