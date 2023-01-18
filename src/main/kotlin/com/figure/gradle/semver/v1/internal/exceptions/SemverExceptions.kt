package com.figure.gradle.semver.v1.internal.exceptions

import com.figure.gradle.semver.v1.internal.git.GitRef

class GitException(t: Throwable) :
    Exception(t)

class UnsupportedBranchException(ref: String) :
    Exception("Unsupported branch: $ref")

class UnexpectedException(message: String) :
    Exception("Something unexpected occurred: $message")

class MissingVersionException(message: String) :
    Exception("Missing version: $message")

class MissingRefException(message: String) :
    Exception("Missing ref: $message")

class MissingBranchMatchingConfigurationException(currentBranch: GitRef.Branch) :
    Exception("Missing branch matching configuration for current branch: ${currentBranch.name}")

class MissingConfigurationException(message: String) :
    Exception("Missing configuration: $message")
