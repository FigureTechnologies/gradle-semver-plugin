/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.URIish
import java.io.File

const val NEXT_PATCH_VERSION = "1.2.1"
const val NEXT_MINOR_VERSION = "1.3.0"
const val NEXT_MAJOR_VERSION = "2.0.0"

fun Git.initializeWithCommitsAndTags(tempRepoDir: File, tempGitRemoteDir: File) {
    // create and commit 3 files in the repository and make a tag after each commit
    for ((minor, i) in (1..3).withIndex()) {
        val tagName = "v1.$minor.0"
        val tagMessage = "Tag $tagName"

        val file = File(tempRepoDir, "file$i.txt")
        file.writeText("This is file $i.")
        add().addFilepattern("file$i.txt").call()

        val author = PersonIdent("Author $i", "author$i@example.com")
        val committer = PersonIdent("Committer $i", "committer$i@example.com")

        val commit: RevCommit = commit().apply {
            this.author = author
            this.committer = committer
            this.message = "Commit $i"
        }.call()

        tag().apply {
            this.name = tagName
            this.tagger = tagger
            this.message = tagMessage
            this.objectId = commit
        }.call()
    }

    // create a temporary Git repository to use as the remote repository
    tempRepoDir.copyRecursively(tempGitRemoteDir)

    // Create a RefSpec to copy all refs starting with refs/heads/* to refs/remotes/origin/*. This mimics how
    // a typical repository looks
    val refSpec = RefSpec("refs/heads/*:refs/remotes/origin/*").setForceUpdate(true)

    remoteSetUrl()
        .setRemoteName("origin")
        .setRemoteUri(URIish(tempGitRemoteDir.toURI().toString()))
        .call()

    push()
        .setRefSpecs(refSpec)
        .setRemote("origin")
        .setPushAll()
        .setPushTags()
        .call()

    fetch().setRefSpecs(refSpec).call()
}
