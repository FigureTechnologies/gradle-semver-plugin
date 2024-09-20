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

import org.gradle.api.logging.Logger

private const val LOG_ERROR_PREFIX = "> ERROR "
private const val LOG_QUIET_PREFIX = "QUIET "
private const val LOG_WARN_PREFIX = "> WARN "
private const val LOG_LIFECYCLE_PREFIX = "> Version: "
private const val LOG_INFO_PREFIX = "INFO "
private const val LOG_DEBUG_PREFIX = "DEBUG "

private fun String.colored(c: String) = "$c$this\u001B[0m"

fun String.darkgray() = this.colored("\u001B[30m")

fun String.red() = this.colored("\u001B[31m")

fun String.green() = this.colored("\u001B[32m")

fun String.yellow() = this.colored("\u001B[33m")

fun String.purple() = this.colored("\u001B[35m")

fun String.lightgray() = this.colored("\u001B[37m")

fun String.bold() = this.colored("\u001B[1m")

fun Logger.error(message: () -> String) =
    this.error(LOG_ERROR_PREFIX.bold() + message().red())

fun Logger.error(throwable: Throwable?, message: () -> String) =
    this.error(LOG_ERROR_PREFIX.bold() + message().red(), throwable)

fun Logger.quiet(message: () -> String) =
    this.quiet(LOG_QUIET_PREFIX.bold() + message().lightgray())

fun Logger.warn(message: () -> String) =
    this.warn(LOG_WARN_PREFIX.bold() + message().yellow())

fun Logger.lifecycle(message: () -> String) =
    this.lifecycle(LOG_LIFECYCLE_PREFIX.bold() + message().purple())

fun Logger.info(message: () -> String) =
    this.info(LOG_INFO_PREFIX.bold() + message().green())

fun Logger.debug(message: () -> String) =
    this.debug(LOG_DEBUG_PREFIX.bold() + message().darkgray())
