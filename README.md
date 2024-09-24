# Semver Gradle Plugin

This Semver Gradle plugin provides a simple approach to
adding semantic versioning to your gradle project using git
history regardless of git strategies.

At a glance, this plugin provides support for the following features:

- Stages (`rc`, `beta`, `stable`, `snapshot`, etc.)
- Modifiers (`auto`, `patch`, `minor`, `major`)
- Branch-based version calculations
- Overriding the version
- Setting an alternate initial version
- Specifying alternate main and development branch names
- Appending build metadata (format: `+<yyyyMMddHHmmss>`)
- Building when
    - No git repository is present
    - No git tags are present
    - No remote branch is present
    - Merging, rebasing, cherry-picking, bisecting, reverting, or in a detached
      head state

## Installation

The following can be added to any of the following:

- `settings.gradle.kts` (recommended)
    - This will automatically apply the version to all projects
- `build.gradle.kts` (root project)
    - This will only automatically apply the version to the root project
- `build.gradle.kts` (subproject)
    - This will only automatically apply the version to the subproject

If the semantic version is targeting the entire project, it's recommended to add
this to the `settings.gradle.kts` file.

```kotlin
plugins {
    id("com.figure.gradle.semver") version "<current_version>"
}
```

## Configuration

> [!IMPORTANT]
> The most minimal configuration is to not provide any configuration at all.
> This will use the default settings and will generate a version based on the
> git history.
>
> However, configurations exist to allow for more control over the versioning
> calculation process.

```kotlin
// For older versions of gradle, you may need to import the configuration method
import com.figure.gradle.semver.semver

// This is purely for example purposes
semver {
    // Default: `settings.settingsDir`
    rootProjectDir = settingsDir.parent

    // Default: `0.0.0` (first build will generate `0.0.1`)
    initialVersion = "1.0.0"

    // No "default", but the plugin will search in order for:
    // `main`, `master
    mainBranch = "trunk"

    // No "default", but the plugin will search in order for:
    // `develop`, `devel`, `dev`
    developmentBranch = "development"

    // Default: `never`
    // Options: `never`, `always`, `locally`
    appendBuildMetadata = "locally"
}
```

## Documentation

For more detailed documentation, please
visit [figuretechnologies.github.io/gradle-semver-plugin](https://figuretechnologies.github.io/gradle-semver-plugin).
