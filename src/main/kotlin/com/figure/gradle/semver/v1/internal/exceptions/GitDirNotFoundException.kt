package com.figure.gradle.semver.v1.internal.exceptions

class GitDirNotFoundException(gitDir: String) : Exception(
    """
        |The directory $gitDir does not exist.
        | If this should be the location of your git directory, please initialize a repo.
        | Alternatively, specify the location of your git directory via `gitDir.set(..)` in the `semver` extension 
        | if, for example, this project is a composite build.
    """.trimMargin().replace("\n", "")
)
