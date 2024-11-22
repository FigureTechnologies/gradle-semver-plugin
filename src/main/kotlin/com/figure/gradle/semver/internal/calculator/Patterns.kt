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
package com.figure.gradle.semver.internal.calculator

internal object Patterns {
    // Numeric identifier pattern. (used for parsing major, minor, and patch)
    private const val NUMERIC = "0|[1-9]\\d*"

    // Alphanumeric or hyphen pattern.
    private const val ALPHANUMERIC_OR_HYPHEN = "[0-9a-zA-Z-]"

    // Letter or hyphen pattern.
    private const val LETTER_OR_HYPHEN = "[a-zA-Z-]"

    // Non-numeric identifier pattern. (used for parsing pre-release)
    private const val NON_NUMERIC = "\\d*$LETTER_OR_HYPHEN$ALPHANUMERIC_OR_HYPHEN*"

    // Dot-separated numeric identifier pattern. (<major>.<minor>.<patch>)
    private const val CORE_VERSION = "($NUMERIC)\\.($NUMERIC)\\.($NUMERIC)"

    // Numeric or non-numeric pre-release part pattern.
    private const val PRE_RELEASE_PART = "(?:$NUMERIC|$NON_NUMERIC)"

    // Pre-release identifier pattern. A hyphen followed by dot-separated
    // numeric or non-numeric pre-release parts.
    private const val PRE_RELEASE = "(?:-($PRE_RELEASE_PART(?:\\.$PRE_RELEASE_PART)*))"

    // Build-metadata identifier pattern. A + sign followed by dot-separated
    // alphanumeric build-metadata parts.
    private const val BUILD = "(?:\\+($ALPHANUMERIC_OR_HYPHEN+(?:\\.$ALPHANUMERIC_OR_HYPHEN+)*))"

    // Version parsing pattern: 1.2.3-alpha+build
    internal const val VERSION_REGEX: String = "$CORE_VERSION$PRE_RELEASE?$BUILD?"
}
