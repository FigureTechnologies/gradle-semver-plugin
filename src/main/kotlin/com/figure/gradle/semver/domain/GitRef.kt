package com.figure.gradle.semver.domain

import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository

sealed interface GitRef {

    companion object {
        val RefHead = Constants.R_HEADS.substringBeforeLast("/")
        val RefRemote = Constants.R_REMOTES.substringBeforeLast("/")
        val RemoteOrigin = "$RefRemote/${Constants.DEFAULT_REMOTE_NAME}"
        val ValidCharacters = """[^0-9A-Za-z\-_.]+""".toRegex()
    }

    data class Branch(
        val name: String, // example: `main`
        val refName: String, // example: `refs/heads/main`
    ) {
        constructor(name: String): this(name, "$RefHead/$name")

        companion object {
            val Main = Branch("main", "$RemoteOrigin/main")
            val Master = Branch("master", "$RemoteOrigin/master")
            val Develop = Branch("develop", "$RemoteOrigin/develop")

            fun headCommitID(repo: Repository, refName: String): ObjectId = repo.findRef(refName).objectId
        }

        fun headCommitID(repo: Repository): ObjectId = repo.findRef(refName).objectId

        fun sanitizedName(): String {
            return name.trim().lowercase().replace(ValidCharacters, "-")
        }
        fun sanitizedNameWithoutPrefix(): String {
            return name.trim().lowercase().replaceBefore("/", "").removePrefix("/").replace(ValidCharacters, "-")
        }
    }
}