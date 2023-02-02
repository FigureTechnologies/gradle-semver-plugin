# Gradle Semver Plugin

This plugin adds a flexible approach to adding semantic versioning to your gradle project using git history.
It supports multi-module gradle projects, running in CI, and multiple types of git strategies.

- [Usage](#usage)
- [Overview](#overview)
- [Using with CI](#using-with-ci)
- [Version Calculation](#version-calculation)
- [Branch Matching Strategy](#branch-matching-strategy)
- [Unsupported](#not-supported)

## Usage

```kotlin build.gradle.kts
plugins {
    id("com.figure.gradle.semver-plugin") version "<latest version>"
}

// The semver extension must be declared before invoking semver.version
semver {
    // All properties are optional, but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, e.g. "overrideVersion" below
    tagPrefix("v")
    initialVersion("0.0.1")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
}

// must be called after semver {}
version = semver.version
```

## Overview
Whenever a gradle task is ran, such as `./gradlew clean build`, the semver plugin will calculate the current semantic version based on git history. 
This calculation is done using:
- The version of the target branch
- The current branch
- The branch matching strategy

This calculated semantic version is then available as an output with the extension properties `semver.version` and `semver.versionTagName`.

### Glossary

| Item                           | Definition                                                                             | Example            | 
|--------------------------------|----------------------------------------------------------------------------------------|--------------------|
| _current branch_               | The branch you are working on.                                                         | `cool-new-feature` |
| _target branch_                | The branch that the current branch targets, often the default branch.                  | `main`             |
| _latest version_               | The latest published git tag on the target branch.                                     | `1.0.2`            |
| _current / calculated version_ | The version that is calculated when it runs. This will be ahead of the latest version. | `1.0.3`            |


### Plugin Extension Properties

These variables come from the plugin extension, and are only available after the `semver {}` extension is configured.

| Variable         | Type     | Description                                                                                  |
|------------------|----------|----------------------------------------------------------------------------------------------|
| `version`        | `String` | The current version, e.g. `1.0.1`                                                            |
| `versionTagName` | `String` | The tag name for the current calculated version, i.e. `tagPrefix` + `version`, e.g. `v1.0.1` |

Example:
```kotlin
semver {
    ...
}
version = semver.version

```

### Plugin Tasks

| Task Name                 | Description                                                                                                                                                                                                              |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `currentSemver`           | Print the current `version` and `versionTagName`                                                                                                                                                                         |
| `generateVersionFile`     | Generate the `build/semver/version.txt` file containing the raw version and the tag version, often used in CI                                                                                                            |
| `createAndPushVersionTag` | Create a git tag from `semver.versionTagName` and push the tag to the remote repo.   Be careful using `:createAndPushVersionTag` in a multi module project as it will attempt to create duplicate tags for each project. |


## Using with CI

When using this plugin in CI, ensure that all branches & tags are checked out to get an accurate version calculation.
By default, the `actions/checkout` action only pulls the latest commit, which can cause some issues with this plugin.

GitHub Actions Example:

```yaml
- name: Checkout
  uses: actions/checkout@v3
  with:
    fetch-depth: 0 # <-- This config is the most important part, and checks out the entire history of the repo
```

## Version Calculation

This plugin comes bundled with a single Version Calculator that implements a target branch calculator - the version of the current
branch is based on the latest version of the branch it targets, e.g. `develop` is branched from `main`, so the version
of `develop` is based on the current version of `main`.

The Target Branch Version Calculator includes two Branch Matching strategies, based on the git strategy that is being used.
By default, the `Flow` strategy is selected if a `develop` branch is present, otherwise the `Flat` strategy will be
used.

- `Flat` - Ideal for projects using a single branch strategy with `main`.
  
| branch | pre release label | target branch | example      |
|--------|-------------------|---------------|--------------|
| `main` |                   | main          | 1.2.3        |
| `xxx`  | xxx               | main          | 1.2.4-xxx.13 |

- `Flow` - Broadly based on a [Git Flow workflow](https://nvie.com/posts/a-successful-git-branching-model/) without
  release branches, the following branches are supported:

| branch             | pre release label | target branch | example       |
|--------------------|-------------------|---------------|---------------|
| `main`             | ''                | main          | 1.2.3         |
| `develop`          | beta              | main          | 1.2.4-beta.13 |
| `hotfix/badthings` | rc                | main          | 1.2.4-rc.2    |
| `xxx`              | xxx               | develop       | 1.2.5-xxx.13  |


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

## Advanced Usage


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


## Not Supported

- Separate versions for subprojects - all subprojects are calculated with the same version

## Gradle

Since this project is a Gradle plugin we are making a best effort to support current Gradle features.
One of the noteable features here is the Gradle configuration-cache.
For the `currentSemver` and `generateVersionFile` tasks, we have updated this plugin to support the configuration-cache.
The `createAndPushVersionTag` however is a little trickier, since we use a third party library for `Git` functions, and
there becomes some incompatibility with a `java.time.Instant` not being cacheable in the state.
We plan to keep working on this to fully support the configuration-cache, but there is currently no timeline for this.

If you'd like to contribute to this effort, please submit a contribution!

```
Configuration cache state could not be cached: field '__git__' from type 'com.figure.gradle.semver.CreateAndPushVersionTag': error writing value of type 'org.eclipse.jgit.api.PushCommand'
> Configuration cache state could not be cached: field 'repo' from type 'org.eclipse.jgit.api.PushCommand': error writing value of type 'org.eclipse.jgit.internal.storage.file.FileRepository'
   > Configuration cache state could not be cached: field 'objectDatabase' from type 'org.eclipse.jgit.internal.storage.file.FileRepository': error writing value of type 'org.eclipse.jgit.internal.storage.file.ObjectDirectory'
      > Configuration cache state could not be cached: field 'config' from type 'org.eclipse.jgit.internal.storage.file.ObjectDirectory': error writing value of type 'org.eclipse.jgit.storage.file.FileBasedConfig'
         > Configuration cache state could not be cached: field 'snapshot' from type 'org.eclipse.jgit.storage.file.FileBasedConfig': error writing value of type 'org.eclipse.jgit.internal.storage.file.FileSnapshot'
            > Configuration cache state could not be cached: field 'lastModified' from type 'org.eclipse.jgit.internal.storage.file.FileSnapshot': error writing value of type 'java.time.Instant'
               > Unable to make private java.lang.Object java.time.Instant.writeReplace() accessible: module java.base does not "opens java.time" to unnamed module @3832230
```
