# Append Build Metadata

Build metadata is a string of characters in the format: `+<yyyyMMddHHmmss>`

To conditionally append build metadata to the next version, use the Gradle
property:

**Via command line:**

```shell
-Psemver.appendBuildMetadata=<appendBuildMetadata>
```

**In any valid `gradle.properties`:**

```properties
semver.appendBuildMetadata=<appendBuildMetadata>
```

???+ note "Important"
    If no value is provided, a default of `never` will be used.

The following are the possible values:

| Modifier  | Description                                                  |
|-----------|--------------------------------------------------------------|
| `never`   | Never adds the generated build metadata                      |
| `always`  | Always adds the generated build metadata                     |
| `locally` | Only adds the generated build metadata when building locally |

### Examples

Latest tag: `v1.0.0`

Current date and time: `01-23-2024 12:34`

| Command                                          | Next Version                                |
|--------------------------------------------------|---------------------------------------------|
| `./gradlew -Psemver.appendBuildMetadata=locally` | 1.0.1+202401231234 (when not running in CI) |
| `./gradlew -Psemver.appendBuildMetadata=always`  | 1.0.1+202401231234                          |
| `./gradlew -Psemver.appendBuildMetadata=never`   | 1.0.1                                       |

Latest tag: `v1.0.0-feat.1` (and still on this feature branch)

Current date and time: `01-23-2024 12:34`

| Command                                          | Next Version                                       |
|--------------------------------------------------|----------------------------------------------------|
| `./gradlew -Psemver.appendBuildMetadata=locally` | 1.0.1-feat.2+202401231234 (when not running in CI) |
| `./gradlew -Psemver.appendBuildMetadata=always`  | 1.0.0-feat.2+202401231234                          |
| `./gradlew -Psemver.appendBuildMetadata=never`   | 1.0.0-feat.2                                       |
