# Modifiers

To alter the next version incrementation, use the Gradle
property: 

**Via command line:**
```shell
-Psemver.modifier=<modifier>
```

**In any valid `gradle.properties`:**
```properties
semver.modifier=<modifier>
```

???+ note
    If no modifier is provided, a default of `auto` will be used.

The following are the possible values:

| Modifier | Description                                                                                      |
|----------|--------------------------------------------------------------------------------------------------|
| `major`  | Increments the major version number                                                              |
| `minor`  | Increments the minor version number                                                              |
| `patch`  | Increments the patch version number                                                              |
| `auto`   | Increments the patch or the pre-release number if the previous tag was a stage-based pre-release |

### Examples

???+ note "Important Note"
    Since no stage is provided in these examples, the default stage of `auto`
    used.

    For how to use with stages, consult the [Modifiers with Stages](modifiers-with-stages.md) documentation.

Latest tag: `v1.0.0-rc.1`

| Command                             | Next Version |
|-------------------------------------|--------------|
| `./gradlew -Psemver.modifier=major` | v2.0.0       |
| `./gradlew -Psemver.modifier=minor` | v1.1.0       |
| `./gradlew -Psemver.modifier=patch` | v1.0.1       |
| `./gradlew -Psemver.modifier=auto`  | v1.0.0-rc.2  |

Latest tag: `v1.0.0`

| Command                             | Next Version |
|-------------------------------------|--------------|
| `./gradlew -Psemver.modifier=major` | v2.0.0       |
| `./gradlew -Psemver.modifier=minor` | v1.1.0       |
| `./gradlew -Psemver.modifier=patch` | v1.0.1       |
| `./gradlew -Psemver.modifier=auto`  | v1.0.1       |

Latest tags (sorted by latest first) and on main branch:

- `v1.0.1-my-feature.1`
- `v1.0.0`

???+ info "Important"
    The latest tag is `v1.0.1-my-feature.1`, however, this is a special
    pre-release type that does not affect the calculation of the
    next version when on a main branch given a modifier.

| Command                             | Next Version |
|-------------------------------------|--------------|
| `./gradlew -Psemver.modifier=major` | v2.0.0       |
| `./gradlew -Psemver.modifier=minor` | v1.1.0       |
| `./gradlew -Psemver.modifier=patch` | v1.0.1       |
| `./gradlew -Psemver.modifier=auto`  | v1.0.1       |

