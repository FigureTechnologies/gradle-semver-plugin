# Gradle Semver Plugin

Opinionated plugin broadly based on a Git Flow workflow without release branches, the following branches are supported:

* main
* develop
* feature/xxx
* hotfix/xxx

## Version Calculation

The semver is calculated primarily based on:
* the current branch
* the scope & stage properties declared for the current branch (or their default values)
                    
### Main Branch
                                                                             
The main branch is the only branch that should be tagged with versions. The calculated version is based on the most recent version on the `main` branch which is then modified based on the scope & stage properties for `main`.
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
main {
    scope("patch")
    stage("beta")
}
```

when the last version tag on main is `v1.2.3` the new version would be calculated as `v1.2.4-beta.1`. The `.1` in the stage indicates there has been 1 commit since the branch point. 

### Feature & Hotfix Branches

Feature & Hotfix branches are similar to `develop` but:
* `feature/xxx` is always branched from `develop` and the version is calculated based on the latest in `develop` 
* `hotfix/xxx` is always branched from `main` and the version is calculated based on the latest in `main`

## Usage

```kotlin
plugins {
    id("io.github.nefilim.gradle.semver-plugin") version "0.0.16"
}

// semver extension must be declared before invoking semver.version()  
semver {
    // all properties are optional but it's a good idea to declare those that you would want  
    // to override with Gradle properties or environment variables
    verbose(true)
    tagPrefix("v")
    initialVersion("0.0.3")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
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

val semVersion = semver.version()
allprojects {
    version = semVersion
}
```

Two extension functions are available on the `semver` extension:

* `version(): String` returns the calculated version
* `versionTagName(): String` returns the tag name for the current calculated version, ie `tagPrefix` + `version()`   

The plugin supports a single task: `cv` that will print out the current calculated version.