# Gradle Semver Plugin

This plugin adds a flexible approach to adding semantic versioning to your gradle project.
It supports multi-module gradle projects, running in CI, and multiple types of git strategies.

- [Version Calculation](#version-calculation)
- [Branch Matching Strategy](#branch-matching-strategy)
- [Usage](#usage)
- [Plugin Extension Properties](#plugin-extension-properties)
- [Plugin Tasks](#plugin-tasks)
- [Using with CI](#using-with-ci)
- [Unsupported](#not-supported)

## Overview

It comes bundled with a single Version Calculator that implements a target branch calculator: the version of the current
branch is based on the latest version of the branch it targets, e.g. `develop` is branched from `main`, thus the version
of `develop` is based on the current version of `main`.

The Target Branch Version Calculator includes two Branch Matching strategies:

- `Flow` - Broadly based on a [Git Flow workflow](https://nvie.com/posts/a-successful-git-branching-model/) without
  release branches, the following branches are supported:

| branch             | pre release label | target branch | example       |
|--------------------|-------------------|---------------|---------------|
| `main`             | ''                | main          | 1.2.3         |
| `develop`          | beta              | main          | 1.2.4-beta.13 |
| `hotfix/badthings` | rc                | main          | 1.2.4-rc.2    |
| `xxx`              | xxx               | develop       | 1.2.5-xxx.13  |

- `Flat` - Ideal for projects using a single branch strategy with `main`.

| branch | pre release label | target branch | example      |
|--------|-------------------|---------------|--------------|
| `main` |                   | main          | 1.2.3        |
| `xxx`  | xxx               | main          | 1.2.4-xxx.13 |

By default, the `Flow` strategy is selected if a `develop` branch is present, otherwise the `Flat` strategy will be
used.

## Version Calculation

The semantic version is calculated primarily based on:

- The version of the target branch
- The current branch
- The branch matching strategy

## Branch Matching Strategy

A Strategy contains a list of `BranchMatchingConfiguration` instances which are applied in order until the first match
is reached, it contains the following properties:

- Branch name regex
- Target branch
- Version modifier: modifies the major, minor or patch components of the semver
- Version qualifier: optionally qualifies the semver with a prerelease label and build metadata

The `VersionModifier` can be set for all `BranchMatchingConfiguration` instances in the strategy with the plugin
extension:

```kotlin
semver {
    versionModifier { nextPatch() }
    // OR
    versionModifier("patch")
}
```

Only a single `BranchMatchingConfiguration` whose regex matches the current branch will be applied, so effectively this
sets the `VersionModifier` for the current branch.

The supported values are `major`, `minor` and `patch`.

## Usage

The plugin is not opinionated on Gradle property names, you can decide your own names and connect them with the DSL.

```kotlin
plugins {
    id("com.figure.gradle.semver-plugin") version "<latest version>"
}

// The semver extension must be declared before invoking semver.version
semver {
    // All properties are optional, but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, e.g. "overrideVersion" below
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }

    // This is only used for non-user defined strategies, i.e. predefined Flow or Flat
    findProperty("semver.modifier")?.toString()
        ?.let { versionModifier(buildVersionModifier(it)) }

    // Manually specifying the gitDir location is typically not necessary. However, in cases where you have a composite
    // gradle build, it will become necessary to define where your .git directory is in correlation to your composite
    // build. In the following example, you may have a build at `parent/child`. `child` specifies that the parent 
    // directory to its projectDir should contain the `.git` directory.
    gitDir("${rootProject.projectDir.parent}/.git")
}

version = semver.version
```

Using a custom Strategy:

```kotlin
semver {
    // All properties are optional, but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, e.g. "overrideVersion" below
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    val semVerModifier = findProperty("semver.modifier")?.toString()
        ?.let { buildVersionModifier(it) } ?: { nextMinor() }

    versionCalculatorStrategy(
        FlatVersionCalculatorStrategy(semVerModifier)
    )
    // OR from scratch - this is rarely used now that Flat and Flow support anything - .*
    versionCalculatorStrategy(
        listOf(
            BranchMatchingConfiguration("""^main$""".toRegex(), GitRef.Branch.Main, { "" to "" }, semVerModifier),
            BranchMatchingConfiguration(
                """.*""".toRegex(),
                GitRef.Branch.Main,
                { preReleaseWithCommitCount(it, GitRef.Branch.Main, it.sanitizedNameWithoutPrefix()) to "" },
                semVerModifier
            ),
        )
    )
}
```

_**PLEASE NOTE:**_ the `semver` extension should be declared **before** the `semver` extension functions are used.

## Plugin Extension Properties

- `version: String` returns the calculated version
- `versionTagName: String` returns the tag name for the current calculated version, ie `tagPrefix` + `version`

## Plugin Tasks

- `printVersion` that will print out the current calculated version
- `generateVersionFile` will generate `build/semver/version.txt` containing the raw version and the tag version
- `createAndPushVersionTag` will create a tag from `semver.versionTagName` and push the tag to the remote repo.
  Be careful using `:createAndPushVersionTag` in a multi module project otherwise it will attempt to create duplicates

## Using with CI

When using this plugin in CI check out all branches & tags to ensure an accurate version calculation.

GitHub Actions Example:

```yaml
- name: Checkout
  uses: actions/checkout@v3
  with:
    fetch-depth: 0 # <-- This piece is the most important part
```

## Not Supported

- Discrete versions for subprojects, all subprojects are calculated with the same version

## Gradle

Since this project is a Gradle plugin we are making a best effort to support current Gradle features.
One of the big ones here is the Gradle configuration-cache.
For the `printVersion` and `generateVersionFile` tasks, we have updated this plugin to support the configuration-cache.
The `createAndPushVersionTag` however is a little trickier, since we use a third party library for `Git` functions, and
there becomes some incompatibility with a `java.time.Instant` not being cacheable in the state.
We plan to keep working on this to fully support the configuration-cache, but there is currently no timeline for this.

If you'd like to contribute to this effort, please contribute!

```
Configuration cache state could not be cached: field '__git__' from type 'com.figure.gradle.semver.CreateAndPushVersionTag': error writing value of type 'org.eclipse.jgit.api.PushCommand'
> Configuration cache state could not be cached: field 'repo' from type 'org.eclipse.jgit.api.PushCommand': error writing value of type 'org.eclipse.jgit.internal.storage.file.FileRepository'
   > Configuration cache state could not be cached: field 'objectDatabase' from type 'org.eclipse.jgit.internal.storage.file.FileRepository': error writing value of type 'org.eclipse.jgit.internal.storage.file.ObjectDirectory'
      > Configuration cache state could not be cached: field 'config' from type 'org.eclipse.jgit.internal.storage.file.ObjectDirectory': error writing value of type 'org.eclipse.jgit.storage.file.FileBasedConfig'
         > Configuration cache state could not be cached: field 'snapshot' from type 'org.eclipse.jgit.storage.file.FileBasedConfig': error writing value of type 'org.eclipse.jgit.internal.storage.file.FileSnapshot'
            > Configuration cache state could not be cached: field 'lastModified' from type 'org.eclipse.jgit.internal.storage.file.FileSnapshot': error writing value of type 'java.time.Instant'
               > Unable to make private java.lang.Object java.time.Instant.writeReplace() accessible: module java.base does not "opens java.time" to unnamed module @3832230
```
