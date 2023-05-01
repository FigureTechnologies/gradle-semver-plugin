/**
 * Copyright (c) 2023 Figure Technologies and its affiliates.
 *
 * This source code is licensed under the Apache 2.0 license found in the
 * LICENSE.md file in the root directory of this source tree.
 */

package com.figure.gradle.semver.functional

import com.figure.gradle.semver.internal.git.GitRef
import com.figure.gradle.semver.internal.git.latestCommitOnBranch
import com.figure.gradle.semver.testkit.GradleFunctionalTestKitExtension
import io.kotest.core.spec.style.FunSpec
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner

class ValidateRepoSpec : FunSpec({
    val runner = GradleRunner.create()

    val gradleFunctionalTestKitExtension = GradleFunctionalTestKitExtension(runner)
    listener(gradleFunctionalTestKitExtension)

    test("validate repo") {
        println("Temp git dir")
        println("--------------")
        gradleFunctionalTestKitExtension.git.repository.refDatabase.refs.forEach {
            println(it.name)
        }

        val latestCommit = gradleFunctionalTestKitExtension.git.latestCommitOnBranch(GitRef.Branch.MAIN)
        val latestCommitViaLog = gradleFunctionalTestKitExtension.git.log().call().first()

        println("--------------")
        println(latestCommit.getOrThrow().name)
        println(latestCommitViaLog.name)

        println("----------------------------------------------------------------------------")

        println("Temp git remote dir")
        println("--------------")
        gradleFunctionalTestKitExtension.git.repository.refDatabase.refs.forEach {
            println(it.name)
        }

        val remoteGit = Git.open(gradleFunctionalTestKitExtension.tempRemoteRepoDir)
        val latestCommitFromRemote = remoteGit.latestCommitOnBranch(GitRef.Branch.MAIN)
        val latestCommitFromRemoteViaLog = remoteGit.log().call().first()

        println("--------------")
        println(latestCommitFromRemote.getOrThrow().name)
        println(latestCommitFromRemoteViaLog.name)
    }

    test("env") {
        System.getenv().toSortedMap().forEach { k, v -> println("$k -- $v") }
        println("------------------------------------------------------------")
        System.getenv().toSortedMap().forEach { k, v ->
            if (k.lowercase().contains("kotest") || v.lowercase().contains("kotest")) {
                println("$k -- $v")
            }
        }
    }
})
