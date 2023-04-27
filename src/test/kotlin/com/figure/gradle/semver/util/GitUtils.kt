/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.transport.URIish
import java.io.File
import kotlin.io.path.createTempDirectory

fun Git.initializeWithCommitsAndTags(directory: File) {
    // create and commit 3 files in the repository and make a tag after each commit
    for ((patch, i) in (1..3).withIndex()) {
        val tagName = "v1.0.$patch"
        val tagMessage = "Tag $tagName"

        val file = File(directory, "file$i.txt")
        file.writeText("This is file $i.")
        add().addFilepattern("file$i.txt").call()

        val author = PersonIdent("Author $i", "author$i@example.com")
        val committer = PersonIdent("Committer $i", "committer$i@example.com")

        val commit = commit().apply {
            this.author = author
            this.committer = committer
            this.message = "Commit $i"
        }.call()

        val tag = tag().apply {
            this.name = tagName
            this.tagger = tagger
            this.message = tagMessage
            this.objectId = commit
        }.call()

        println("Created tag: ${tag.name}")
    }

    // create a temporary Git repository to use as the remote repository
    val tempRemoteDir = createTempDirectory("tempRepoRemote").toFile()
    Git.init().setDirectory(tempRemoteDir).setBare(true).call()

    // set up a remote URL to point to the temporary Git repository
    val remoteUrl = URIish(tempRemoteDir.absolutePath)

    remoteAdd()
        .setName("origin")
        .setUri(remoteUrl)
        .call()
}
