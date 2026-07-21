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
package com.figure.gradle.semver.internal

import com.figure.gradle.semver.internal.calculator.VersionFactoryContext
import com.figure.gradle.semver.internal.calculator.calculateNextVersion
import org.gradle.api.provider.Provider
import java.io.Serializable

/**
 * Lazy [project.version] holder so the version string is not resolved during configuration.
 *
 * Prefer reading via [versionProvider] (or `semver.version`) for task inputs. Calling [toString]
 * during configuration reintroduces the version as a configuration-cache input.
 */
class DeferredSemverVersion(
    private val context: VersionFactoryContext,
    @Transient
    private val versionProvider: Provider<String>? = null,
) : Serializable {
    override fun toString(): String =
        versionProvider?.get() ?: calculateNextVersion(context)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
