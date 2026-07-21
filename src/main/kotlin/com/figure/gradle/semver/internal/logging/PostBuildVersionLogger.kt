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
package com.figure.gradle.semver.internal.logging

import com.figure.gradle.semver.internal.extensions.flowScope
import com.figure.gradle.semver.internal.extensions.projectDir
import com.figure.gradle.semver.internal.writer.semverPropertiesFile
import com.figure.gradle.semver.internal.writer.writeVersionToPropertiesFile
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.always
import java.io.File

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

fun PluginAware.registerPostBuildVersionActions(
    version: Provider<String>,
    tagPrefix: Provider<String>,
) {
    val propertiesFile = semverPropertiesFile(projectDir)
    flowScope.always(PostBuildVersionAction::class) { action ->
        action.parameters.version.set(version)
        action.parameters.tagPrefix.set(tagPrefix)
        action.parameters.propertiesFilePath.set(propertiesFile.absolutePath)
    }
}

private abstract class PostBuildVersionAction : FlowAction<PostBuildVersionAction.Params> {
    interface Params : FlowParameters {
        @get:Input
        val version: Property<String>

        @get:Input
        val tagPrefix: Property<String>

        @get:Input
        val propertiesFilePath: Property<String>
    }

    override fun execute(parameters: Params) {
        val nextVersion = parameters.version.get()
        val prefix = parameters.tagPrefix.get()

        log.lifecycle { nextVersion }

        writeVersionToPropertiesFile(
            propertiesFile = File(parameters.propertiesFilePath.get()),
            version = nextVersion,
            tagPrefix = prefix,
        )
    }
}
