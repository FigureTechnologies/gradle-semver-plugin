# Quick Start

This Gradle plugin calculates the next version based on the latest Git tag,
using optional parameters and properties.

Basic usage:

```shell
./gradlew build
```

With parameters:

```shell
./gradlew build -Psemver.stage=rc -Psemver.modifier=minor
```

## Parameters and Properties

The plugin uses several parameters and properties to calculate the next version.
Default values and valid inputs vary. For detailed information on each parameter
and property, including usage examples and valid values, please refer to the
following documentation pages:

1. [semver.stage](stages.md)
2. [semver.modifier](modifiers.md)
3. [semver.overrideVersion](override-version.md)
4. [semver.forMajorVersion](for-major-version.md)
5. [semver.tagPrefix](tag-prefix.md)
6. [semver.appendBuildMetadata](append-build-metadata.md)

???+ info "Important!"
    Each of these can be set as:

    - Gradle parameters (`-P` on the command-line)
    - in any valid `gradle.properties` files, in search order:
        - in the Gradle user home directory (
          typically `~/.gradle/gradle.properties`)
        - in the Gradle home directory
        - in the project's directory where the plugin is applied

