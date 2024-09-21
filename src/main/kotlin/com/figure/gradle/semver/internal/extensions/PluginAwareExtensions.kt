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

import org.gradle.api.Project
import org.gradle.api.flow.FlowScope
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.File

val PluginAware.providers: ProviderFactory
    get() =
        when (this) {
            is Settings -> providers
            is Project -> providers
            else -> error("Not a project or settings")
        }

val PluginAware.rootDir: File
    get() =
        when (this) {
            is Settings -> settingsDir
            is Project -> rootDir
            else -> error("Not a project or settings")
        }

val PluginAware.projectDir: File
    get() =
        when (this) {
            is Settings -> settingsDir
            is Project -> projectDir
            else -> error("Not a project or settings")
        }

val PluginAware.gradle: Gradle
    get() =
        when (this) {
            is Settings -> gradle
            is Project -> gradle
            else -> error("Not a project or settings")
        }

val PluginAware.extensions: ExtensionContainer
    get() =
        when (this) {
            is Settings -> extensions
            is Project -> extensions
            else -> error("Not a project or settings")
        }

val PluginAware.flowScope: FlowScope
    get() =
        when (this) {
            is Project -> serviceOf<FlowScope>()
            is Settings -> serviceOf<FlowScope>()
            else -> error("Not a project or settings")
        }
