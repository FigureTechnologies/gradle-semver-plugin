# Gradle Semver Plugin

* [Overview](#overview)
* [Version Calculation](#version-calculation)
* [Branch Matching Strategy](#branch-matching-strategy)
* [Usage](#usage)
* [Plugin Extension Properties](#plugin-extension-properties)
* [Plugin Tasks](#plugin-tasks)
* [Using with CI/CD](#using-with-cicd)
* [Unsupported](#not-supported)     

## Overview 

A Gradle plugin with a flexible approach to generating semantic versions, typically for use within a Gradle project. It supports running under Github Actions. 

It comes bundled with a single Version Calculator that implements a target branch calculator: the version of the current branch is based on the latest version of the branch it targets, eg `develop` is branched from `main`, thus the version of `develop` is based on the current version of `main`. 

The Target Branch Version Calculator includes two Branch Matching strategies: 
* Flow - broadly based on a [Git Flow workflow](https://nvie.com/posts/a-successful-git-branching-model/) without release branches, the following branches are supported:
  |branch|pre release label|target branch|example|
  |------|-----------------|-------------|-------|
  |`main`| |main|1.2.3|
  |`develop`|beta|main|1.2.4-beta.13|
  |`xxx`|xxx|develop|1.2.5-xxx.13|
  |`hotfix/badthings`|rc|main|1.2.4-rc.2|
* Flat - ideal for simpler projects without an integration branch such as `develop`:
  |branch|pre release label|target branch|example|
  |------|-----------------|-------------|-------|
  |`main`| |main|1.2.3|
  |`xxx`|xxx|main|1.2.4-xxx.13|

The `Flow` strategy is automatically selected if a `develop` branch is present, otherwise the `Flat` strategy is selected.

## Version Calculation

The semver is calculated primarily based on:
* the version of the target branch
* the current branch
* the branchMatching strategy 

## Branch Matching Strategy

A Strategy contains a list of `BranchMatchingConfiguration` instances which are applied in order until the first match is reached, it contains the following properties:
  * branch name regex
  * target branch
  * version modifier: modifies the major, minor or patch components of the semver
  * version qualifier: optionally qualifies the semver with a prerelease label and build metadata

The `VersionModifier` can be set for every `BranchMatchingConfiguration` instance in the strategy with the plugin extension:

```kotlin
semver {
    versionModifier { nextPatch() }
    // OR
    versionModifier("patch")
}
```
Only a single `BranchMatchingConfiguration` which regex matches the current branch will be applied, so effectively this sets the `VersionModifier` for the current branch.

The supported values are `major`, `minor` and `patch`. 

## Usage

The plugin is not opinionated on Gradle property names, you can decide your own names and connect them with the DSL.

```kotlin
plugins {
    id("com.figure.gradle.semver-plugin") version "<latest version>"
}

// semver extension must be declared before invoking semver.version()  
semver {
    // all properties are optional but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, eg "overrideVersion" below
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    findProperty("semver.modifier")?.toString()?.let { versionModifier(buildVersionModifier(it)) } // this is only used for non user defined strategies, ie predefined Flow or Flat
}

version = semver.version
```
Using a custom Strategy: 
```kotlin
semver {
    // all properties are optional but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, eg "overrideVersion" below
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    val semVerModifier = findProperty("semver.modifier")?.toString()?.let { buildVersionModifier(it) } ?: { nextMinor() }
    versionCalculatorStrategy(
        FlatVersionCalculatorStrategy(semVerModifier)
    )
    // OR from scratch:
    versionCalculatorStrategy(
        listOf(
            BranchMatchingConfiguration("""^main$""".toRegex(), GitRef.Branch.Main, { "" to "" }, semVerModifier),
            BranchMatchingConfiguration(""".*""".toRegex(), GitRef.Branch.Main, { preReleaseWithCommitCount(it, GitRef.Branch.Main, it.sanitizedNameWithoutPrefix()) to "" }, semVerModifier),
        )
    )
}
```

_**PLEASE NOTE:**_ the `semver` stanza should be declared **before** the `semver` extension functions are used.

## Plugin Extension Properties
* `version: String` returns the calculated version
* `versionTagName: String` returns the tag name for the current calculated version, ie `tagPrefix` + `version`   

## Plugin Tasks 
* `cv` that will print out the current calculated version
* `generateVersionFile` will generate `build/semver/version.txt` containing the raw version and the tag version
* `createAndPushVersionTag` will create a tag from `semver.versionTagName` and push the tag to the remote repo, take care to use `:createAndPushVersionTag` in a multi module project otherwise it will attempt to create duplicates 

## Using with CI/CD

Make sure to check out all branches & tags, eg using GitHub Actions:

      - name: Checkout
        uses: actions/checkout@v2
        with:
          fetch-depth: 0

## Not Supported
* Discrete versions for sub projects, all subprojects are calculated with the same version
