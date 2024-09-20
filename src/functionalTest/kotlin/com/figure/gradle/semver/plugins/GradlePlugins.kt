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
package com.figure.gradle.semver.plugins

import com.autonomousapps.kit.AbstractGradleProject.Companion.PLUGIN_UNDER_TEST_VERSION
import com.autonomousapps.kit.gradle.Plugin

object GradlePlugins {
    val KOTLIN_VERSION: String = KotlinVersion.CURRENT.toString()
    val semverPluginId = "com.figure.gradle.semver"

    val semverPlugin: Plugin = Plugin(semverPluginId, PLUGIN_UNDER_TEST_VERSION)

    val kotlinNoVersion: Plugin = Plugin("org.jetbrains.kotlin.jvm", null, true)
    val kotlinNoApply: Plugin = Plugin("org.jetbrains.kotlin.jvm", KOTLIN_VERSION, false)
}
