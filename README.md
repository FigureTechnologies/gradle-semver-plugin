# Gradle Semver Plugin

Opinionated plugin broadly based on a [Git Flow workflow](https://nvie.com/posts/a-successful-git-branching-model/) without release branches, the following branches are supported:

* main
* develop
* feature/xxx
* hotfix/xxx

## Version Calculation

The semver is calculated primarily based on:
* the version of the base branch
* the current branch
* the scope & stage properties declared for the current branch (or their default values)
                    
### Main Branch
                                                                             
_**The main branch is the only branch that should be tagged with versions.**_ The calculated version is based on the most recent version on the `main` branch which is then modified based on the scope & stage properties for `main`.
eg. given 

```kotlin
main {
    scope("patch")
    stage("final")
}
```

when the last version tag on main is `v1.2.3` the new version would be calculated as `v1.2.4` 

### Develop Branch

The `develop` branch should be rebased from main before releasing (eg locally or to a staging system). 
The calculated version is based on the most recent version on the `main` branch which is then modified based on the scope & stage properties for `develop`.
eg. given

```kotlin
develop {
    scope("patch")
    stage("beta")
}
```

when the last version tag on main is `v1.2.3` the new version would be calculated as `v1.2.4-beta.1`. The `.1` in the stage indicates there has been 1 commit since the branch point. 

### Feature & Hotfix Branches

Feature & Hotfix branches are similar to `develop` but:
* `feature/xxx` is always branched from `develop` and the version is calculated based on the latest in `develop` 
  * eg, if main is at `v1.2.3`, develop is at `v1.2.4-beta.0`, the feature branch could be `v1.2.5-alpha.0`
* `hotfix/xxx` is always branched from `main` and the version is calculated based on the latest in `main`
            
Feature branches can be customized with a regex to match the branch name, eg:

`featureBranchRegex(listOf("[a-zA-Z\\-_0-9]+\\/sc-\\d+\\/[a-zA-Z\\-_0-9]+"))` will allow `peter/sc-123123/add_new_feature` to be considered as a feature branch.

## Usage

```kotlin
plugins {
    id("io.github.nefilim.gradle.semver-plugin") version "0.0.22"
}

// semver extension must be declared before invoking semver.version()  
semver {
    // all properties are optional but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables, eg "overrideVersion" below
    verbose(true)
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    featureBranchRegex(listOf("[a-zA-Z\\-_0-9]+\\/sc-\\d+\\/[a-zA-Z\\-_0-9]+"))
    main {
        scope(findProperty("semver.main.scope")?.toString() ?: "patch")
        stage(findProperty("semver.main.stage")?.toString() ?: "final")
    }
    develop {
        scope("patch")
        stage("beta")
    }
    feature {
        scope("patch")
        stage("alpha")
    }
    hotfix {
        scope("patch")
        stage("rc")
    }
}

version = semver.version()
```

_**PLEASE NOTE:**_ the `semver` stanza should be declared **before** the `semver` extension functions are used.

Two extension functions are available on the `semver` extension:

* `version(): String` returns the calculated version
* `versionTagName(): String` returns the tag name for the current calculated version, ie `tagPrefix` + `version()`   

The plugin supports a single task: `cv` that will print out the current calculated version.
