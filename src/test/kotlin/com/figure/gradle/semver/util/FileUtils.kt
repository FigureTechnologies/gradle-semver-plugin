/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.util

import java.io.File
import java.io.FileWriter
import java.nio.file.NotDirectoryException

fun resourceFromPath(resourcePath: String): File =
    File(Thread.currentThread().contextClassLoader?.getResource(resourcePath)?.toURI()!!)

fun String.toFile(path: String): File {
    val newFile = File(path)
    newFile.parentFile.mkdirs()
    newFile.writeText(this)
    return newFile
}

fun File.copyToDir(dest: File, fileName: String) {
    if (dest.isDirectory) {
        val newFile = File("${dest.path}/$fileName")
        this.copyTo(newFile)
    } else {
        throw NotDirectoryException(dest.path)
    }
}

fun File.appendFileContents(newContent: String): File {
    FileWriter(this).use {
        it.write(newContent)
    }
    return this
}
