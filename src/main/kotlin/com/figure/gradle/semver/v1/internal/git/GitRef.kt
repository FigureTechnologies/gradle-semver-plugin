/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.v1.internal.git

import org.eclipse.jgit.lib.Constants

sealed interface GitRef {
    companion object {
        val REF_HEAD = Constants.R_HEADS.substringBeforeLast("/")
        val REF_REMOTE = Constants.R_REMOTES.substringBeforeLast("/")
        val REMOTE_ORIGIN = "$REF_REMOTE/${Constants.DEFAULT_REMOTE_NAME}"
        val VALID_CHARACTERS = """[^0-9A-Za-z\-_.]+""".toRegex()
    }

    data class Branch(
        val name: String, // example: `main`
        val refName: String, // example: `refs/heads/main`
    ) {
        constructor(name: String) : this(name, "$REF_HEAD/$name")

        companion object {
            val MAIN = Branch("main", "$REMOTE_ORIGIN/main")
            val MASTER = Branch("master", "$REMOTE_ORIGIN/master")
            val DEVELOP = Branch("develop", "$REMOTE_ORIGIN/develop")
        }

        fun sanitizedNameWithoutPrefix(): String =
            name.trim()
                .lowercase()
                .replaceBefore("/", "")
                .removePrefix("/")
                .replace(VALID_CHARACTERS, "-")
    }
}
