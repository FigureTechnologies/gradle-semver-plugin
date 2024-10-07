# For Major Version

To target a historical major version line (useful for applying fixes to older
major versions), use the Gradle property:

**Via command line:**

```shell
-Psemver.forMajorVersion=<forMajorVersion>
```

**In any valid `gradle.properties`:**

```properties
semver.forMajorVersion=<forMajorVersion>
```

### Examples

Latest tag: `v2.0.0`
Latest v1 tag: `v1.5.9`

???+ note "Important Note"
If no stage or modifier is provided, a default of `auto` used.

| Command                                                                             | Next Version |
|-------------------------------------------------------------------------------------|--------------|
| `./gradlew -Psemver.forMajorVersion=1`                                              | 1.5.10       |
| `./gradlew -Psemver.forMajorVersion=1 -Psemver.modifier=minor`                      | 1.6.0        |
| `./gradlew -Psemver.forMajorVersion=1 -Psemver.modifier=minor -Psemver.modifier=rc` | 1.6.0-rc.1   |

### Suggested Workflow

1. Identify and checkout the latest tag for the major version you want to
   target.
2. Create a new branch for your changes off the tag (e.g. `release/v1.x`).
3. Make your changes and commit them to your new branch.
4. Execute gradle build, publish, and release steps with
   `-Psemver.forMajorVersion=<your-major-version>` to target the historical
   major version line.
