# Gradle Semver Plugin
     
A Gradle plugin with a flexible approach to generating semantic versions, typically for use within a Gradle project. 

It comes bundled with a single Version Calculator that implements a target branch strategy: the version of the current branch is based on the latest version of the branch it targets, eg `develop` is branched from `main`, thus the version of `develop` is based on the current version of `main`. 

The Target Branch Version Calculator includes two Branch Matching strategies: 
* Flow - broadly based on a [Git Flow workflow](https://nvie.com/posts/a-successful-git-branching-model/) without release branches, the following branches are supported:
  * `main`: no prerelease label in the semver, eg: `1.2.3`
  * `develop`: `beta` prerelease label in the semver, target branch: `main`, eg: `1.2.4-beta.13`
  * `feature/mycool_feature`: `mycool_feature` prerelease label in the semver, target branch: `develop`, eg: `1.2.5-mycool_feature.1` 
  * `hotfix/badthings`: `rc` prerelease label in the semver, target branch: `main`, eg `1.2.4-rc.2`
* Flat - ideal for simpler projects without a `develop` branch:
  * `main`: no prerelease label in the semver, eg: `1.2.3`
  * `xxx`: `xxx` prerelease label in the semver, target branch: `main`, eg: `1.2.4-beta.13`

The `Flow` strategy is automatically selected if a `develop` branch is present, otherwise the `Flat` strategy is selected.

## Version Calculation

The semver is calculated primarily based on:
* the version of the target branch
* the current branch
* the branchMatching strategy 

## Branch Matching Strategy

A Strategy contains a list of `BranchMatchingConfiguration` instances which are applied in order until the first match is reached (recommend the last entry's regex to be `.*`), it contains the following properties:
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
    id("io.github.nefilim.gradle.semver-plugin") version "<latest version>"
}

// semver extension must be declared before invoking semver.version()  
semver {
    // all properties are optional but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, eg "overrideVersion" below
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    findProperty("semver.modifier")?.toString()?.let { versionModifier(it) } 
}

version = semver.version
```

_**PLEASE NOTE:**_ the `semver` stanza should be declared **before** the `semver` extension functions are used.

Two extension properties are available on the `semver` extension:

* `version: String` returns the calculated version
* `versionTagName: String` returns the tag name for the current calculated version, ie `tagPrefix` + `version`   

The plugin supports two tasks: 
* `cv` that will print out the current calculated version
* `generateVersionFile` will generate `build/semver/version.txt` containing the raw version and the tag version
* `createAndPushVersionTag` will create a tag from `semver.versionTagName` and push the tag to the remote repo, take care to use `:createAndPushVersionTag` in a multi module project otherwise it will attempt to create duplicates 