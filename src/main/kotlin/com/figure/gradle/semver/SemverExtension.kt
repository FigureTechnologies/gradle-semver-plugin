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
package com.figure.gradle.semver

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property

/**
 * Configuration for the Semver Settings Plugin that enables:
 * - Manually setting the rootProjectDir
 * - An initial version
 * - The main branch, if not `main` or `master`
 * - The development branch if not `develop`, `devel`, or `dev`
 *
 */
interface SemverExtension {
    val rootProjectDir: RegularFileProperty
    val initialVersion: Property<String>
    val mainBranch: Property<String>
    val developmentBranch: Property<String>
    val appendBuildMetadata: Property<String>

    companion object {
        const val NAME = "semver"
    }
}

fun Settings.semver(configure: SemverExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure(SemverExtension.NAME, configure)
