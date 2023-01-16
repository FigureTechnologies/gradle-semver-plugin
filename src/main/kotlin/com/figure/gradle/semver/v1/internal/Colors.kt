package com.figure.gradle.semver.v1.internal

private fun String.colored(c: String) = "$c$this\u001B[0m"
internal fun String.green() = this.colored("\u001B[32m")
internal fun String.red() = this.colored("\u001B[31m")
internal fun String.purple() = this.colored("\u001B[35m")
internal fun String.yellow() = this.colored("\u001B[33m")
internal fun String.bold() = this.colored("\u001B[1m")
