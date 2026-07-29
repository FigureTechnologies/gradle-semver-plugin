# Semver Gradle Plugin

Semantic versions from your git history — across strategies, branches, and the messy states real builds hit.

## Why

- **Stages and modifiers** — ship `rc`, `beta`, `SNAPSHOT`, and friends; bump with `major` / `minor` / `patch`, or default `auto` (continues a matching pre-release)
- **Branch-aware** — next version follows how you actually branch, not a single rigid workflow
- **Built for real repos** — works with missing tags or remotes, odd git states, and Gradle configuration cache

## Install

Add the plugin to `settings.gradle.kts` (recommended — versions every project). You can also apply it in a root or subproject `build.gradle.kts`.

```kotlin
plugins {
    id("com.figure.gradle.semver") version "<current_version>"
}
```

## Use

Zero config is enough. Build and the plugin calculates the next version from your latest git tag:

```shell
./gradlew build
```

Shape the next version with properties:

```shell
./gradlew build -Psemver.stage=rc -Psemver.modifier=minor
```

Same properties work in `gradle.properties`. Full list — including override, tag prefix, and more — lives in the [docs](https://figuretechnologies.github.io/gradle-semver-plugin/quick-start/).

## Configure (optional)

No `semver { }` block required. Defaults already produce a version from git history. Override only what you need:

```kotlin
semver {
    // Default: 0.0.0 (first build → 0.0.1)
    initialVersion = "1.0.0"

    // Searched in order if unset: main, master
    mainBranch = "trunk"

    // Searched in order if unset: develop, devel, dev
    developmentBranch = "development"

    // Default: never — options: never, always, locally
    appendBuildMetadata = "locally"
}
```

## Configuration cache

Version calculation is lazy so new commits do not discard the configuration cache. Prefer `semver.version` / `semver.versionTag` for task inputs. Details: [configuration cache](https://figuretechnologies.github.io/gradle-semver-plugin/configuration-cache/).

## Docs

Full reference: [figuretechnologies.github.io/gradle-semver-plugin](https://figuretechnologies.github.io/gradle-semver-plugin).
