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

// In order from lowest to highest priority
enum class Stage(val value: String) {
    Dev("dev"),
    Alpha("alpha"),
    Beta("beta"),
    ReleaseCandidate("rc"),
    Snapshot("SNAPSHOT"),
    Final("final"),
    GA("ga"),
    Release("release"),
    Stable("stable"),
    Auto("auto"),
    ;

    companion object {
        fun fromValue(value: String): Stage =
            entries.find { it.value.lowercase() == value.lowercase() }
                ?: error("Invalid stage provided: $value. Valid values are: ${entries.joinToString { it.value.lowercase() }}")
    }
}
