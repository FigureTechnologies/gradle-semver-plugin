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
| `./gradlew -Psemver.forMajorVersion=1` -Psemver.modifier=minor                      | 1.6.0        |
| `./gradlew -Psemver.forMajorVersion=1` -Psemver.modifier=minor -Psemver.modifier=rc | 1.6.0-rc.1   |
