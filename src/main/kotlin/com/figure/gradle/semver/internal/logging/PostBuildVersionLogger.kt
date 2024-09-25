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
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.always

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)

fun PluginAware.registerPostBuildVersionLogMessage(message: String) {
    flowScope.always(PostBuildVersionLogger::class) { action ->
        action.parameters.message.set(message)
    }
}

private abstract class PostBuildVersionLogger : FlowAction<PostBuildVersionLogger.Params> {
    interface Params : FlowParameters {
        @get:Input
        val message: Property<String>
    }

    override fun execute(parameters: Params) {
        log.lifecycle { parameters.message.get() }
    }
}
