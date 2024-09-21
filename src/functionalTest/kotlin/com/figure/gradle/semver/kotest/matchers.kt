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
package com.figure.gradle.semver.kotest

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch

infix fun <T : Any> Iterable<T>.shouldOnlyHave(t: T): Iterable<T> = this.map { it shouldBe t }

infix fun Iterable<String>.shouldOnlyContain(t: String): Iterable<String?> = this.map { it shouldContain t }

infix fun Iterable<String>.shouldOnlyMatch(t: Regex): Iterable<String?> = this.map { it shouldMatch t }
