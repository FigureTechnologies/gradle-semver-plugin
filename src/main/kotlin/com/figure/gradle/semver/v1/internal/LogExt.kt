package com.figure.gradle.semver.v1.internal

import org.gradle.api.logging.Logger

private const val LOG_ERROR_PREFIX = "[gradle-semver-plugin] ERROR "
private const val LOG_QUIET_PREFIX = "[gradle-semver-plugin] QUIET "
private const val LOG_WARN_PREFIX = "[gradle-semver-plugin] WARN "
private const val LOG_LIFECYCLE_PREFIX = "Semver > "
private const val LOG_INFO_PREFIX = "[gradle-semver-plugin] INFO "
private const val LOG_DEBUG_PREFIX = "[gradle-semver-plugin] DEBUG "

private fun String.colored(c: String) = "$c$this\u001B[0m"
internal fun String.darkgray() = this.colored("\u001B[30m")
internal fun String.red() = this.colored("\u001B[31m")
internal fun String.green() = this.colored("\u001B[32m")
internal fun String.yellow() = this.colored("\u001B[33m")
internal fun String.purple() = this.colored("\u001B[35m")
internal fun String.lightgray() = this.colored("\u001B[37m")
internal fun String.bold() = this.colored("\u001B[1m")

internal fun Logger.semverError(message: String) = this.error(LOG_ERROR_PREFIX.bold() + message.red())
internal fun Logger.semverError(message: String, e: Throwable) = this.error(LOG_ERROR_PREFIX.bold() + message.red(), e)

internal fun Logger.semverQuiet(message: String) = this.quiet(LOG_QUIET_PREFIX.bold() + message.lightgray())

internal fun Logger.semverWarn(message: String) = this.warn(LOG_WARN_PREFIX.bold() + message.yellow())

internal fun Logger.semverLifecycle(message: String) = this.lifecycle(LOG_LIFECYCLE_PREFIX.bold() + message.purple())

internal fun Logger.semverInfo(message: String) = this.info(LOG_INFO_PREFIX.bold() + message.green())

internal fun Logger.semverDebug(message: String) = this.debug(LOG_DEBUG_PREFIX.bold() + message.darkgray())
