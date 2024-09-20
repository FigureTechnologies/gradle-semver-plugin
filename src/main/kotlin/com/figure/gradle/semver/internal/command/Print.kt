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

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val log = Logging.getLogger(Logger.ROOT_LOGGER_NAME)
private const val SHORTENED_COMMIT_LENGTH = 7

class Print(
    private val kgit: KGit,
) {
    fun tags(debug: Boolean) {
        val logLevel = if (debug) LogLevel.LIFECYCLE else LogLevel.INFO
        log.log(logLevel, "Tags:")
        kgit.tags().forEach { tag ->
            log.log(logLevel, "  ${tag.name}")
        }
    }

    fun refs(debug: Boolean) {
        val logLevel = if (debug) LogLevel.LIFECYCLE else LogLevel.INFO
        log.log(logLevel, "Refs:")
        kgit.git.repository.refDatabase.refs.forEach { ref ->
            log.log(logLevel, "  ${ref.name}")
        }
    }

    fun commits(debug: Boolean) {
        val logLevel = if (debug) LogLevel.LIFECYCLE else LogLevel.INFO
        log.log(logLevel, "Commits:")
        kgit.log().forEach { commit ->
            log.log(
                logLevel,
                "  ${commit.name.take(SHORTENED_COMMIT_LENGTH)} ${convertEpochToCustomFormat(commit.commitTime)} ${commit.shortMessage}",
            )
        }
    }

    private fun convertEpochToCustomFormat(epochTime: Int): String {
        val instant = Instant.ofEpochSecond(epochTime.toLong())
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yy HH:mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}
