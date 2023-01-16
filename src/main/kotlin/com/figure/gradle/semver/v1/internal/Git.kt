package com.figure.gradle.semver.v1.internal

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project

internal fun Project.git(gitDir: String): Git =
    Git(
        FileRepositoryBuilder()
            .setGitDir(file(gitDir))
            .readEnvironment()
            .findGitDir()
            .build()
    )

internal fun Project.hasGit(gitDir: String): Boolean =
    file(gitDir).exists()
