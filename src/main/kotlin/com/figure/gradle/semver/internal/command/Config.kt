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
package com.figure.gradle.semver.internal.command

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants

class Config(
    private val git: Git,
) {
    fun author(name: String, email: String) {
        val config = git.repository.config

        config.setString(
            ConfigConstants.CONFIG_USER_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_NAME,
            name,
        )
        config.setString(
            ConfigConstants.CONFIG_USER_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_EMAIL,
            email,
        )

        config.save()
    }
}
