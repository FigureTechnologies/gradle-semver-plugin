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
package com.figure.gradle.semver.internal.extensions

import org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME
import org.eclipse.jgit.lib.Constants.R_HEADS
import org.eclipse.jgit.lib.Constants.R_REMOTES
import org.eclipse.jgit.lib.Ref

const val R_REMOTES_ORIGIN = "$R_REMOTES$DEFAULT_REMOTE_NAME"

private val validCharacters: Regex = """[^0-9A-Za-z\-_.]+""".toRegex()

fun Ref.prereleaseLabel(): String =
    name.trim()
        .lowercase()
        .replace(R_HEADS, "")
        .replace("$R_REMOTES_ORIGIN/", "")
        .removePrefix("/")
        .replace(validCharacters, "-")

fun String.shortName(): String =
    trim()
        .lowercase()
        .replace(R_HEADS, "")
        .replace("$R_REMOTES_ORIGIN/", "")
        .removePrefix("/")
