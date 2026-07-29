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
package com.figure.gradle.semver.internal.properties

import io.github.z4kn4fein.semver.Inc

enum class Modifier(val value: String) {
    Major("major"),
    Minor("minor"),
    Patch("patch"),
    Auto("auto"),
    ;

    fun toInc(): Inc = when (this) {
        Major -> Inc.MAJOR
        Minor -> Inc.MINOR
        Patch -> Inc.PATCH
        Auto -> Inc.PATCH
    }

    companion object {
        fun fromValue(value: String): Modifier =
            entries.find { it.value.lowercase() == value.lowercase() }
                ?: error("Invalid modifier provided: $value. Valid values are: ${entries.joinToString { it.value }}")
    }
}
