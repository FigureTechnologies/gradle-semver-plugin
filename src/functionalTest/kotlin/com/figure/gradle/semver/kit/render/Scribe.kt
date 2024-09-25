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
package com.figure.gradle.semver.kit.render

import com.autonomousapps.kit.GradleProject

// TODO: Switch to official version when new version is released
public class Scribe @JvmOverloads constructor(
    /** Which Gradle DSL to use for rendering. */
    public val dslKind: GradleProject.DslKind = GradleProject.DslKind.KOTLIN,
    /** Indent level when entering a block. */
    public val indent: Int = 2,
) : AutoCloseable {
    private val buffer = StringBuilder()

    /** Starting indent for any block. */
    private var start: Int = 0

    /** Enter a block, increase the indent. */
    private fun enter() {
        start += indent
    }

    /** Exit a block, decrease the indent. */
    private fun exit() {
        start -= indent
    }

    override fun close() {
        buffer.clear()
        start = 0
    }

    public fun block(
        element: Element.Block,
        block: (Scribe) -> Unit,
    ): String {
        // e.g., "plugins {"
        indent()
        buffer.append(element.name)
        buffer.appendLine(" {")

        // increase the indent
        enter()

        // write the block inside the {}
        block(this)

        // decrease the indent
        exit()

        // closing brace
        indent()
        buffer.appendLine("}")

        // return the string
        return buffer.toString()
    }

    public fun line(
        block: (Scribe) -> Unit,
    ): String {
        indent()
        block(this)
        buffer.appendLine()

        return buffer.toString()
    }

    public fun append(obj: Any?) {
        buffer.append(obj.toString())
    }

    public fun appendLine() {
        buffer.appendLine()
    }

    private fun indent() {
        buffer.append(" ".repeat(start))
    }

    public fun appendQuoted(obj: Any?) {
        append(quote())
        append(obj.toString())
        append(quote())
    }

    private fun quote(): String = if (dslKind == GradleProject.DslKind.GROOVY) "'" else "\""
}
