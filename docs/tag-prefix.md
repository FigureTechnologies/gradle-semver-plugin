# Tag Prefix

To alter the next version tag prefix, use the Gradle property:

**Via command line:**

```shell
-Psemver.tagPrefix=<tagPrefix>
```

**In any valid `gradle.properties`:**

```properties
semver.tagPrefix=<tagPrefix>
```

???+ note
    If no tag prefix is provided, a default of `v` will be used.

### Examples

Latest tag: `v1.0.0`

| Command                                     | Next Version      |
|---------------------------------------------|-------------------|
| `./gradlew -Psemver.tagPrefix=my-prefix`    | my-prefix1.0.1    |
| `./gradlew -Psemver.tagPrefix=support-lib-` | support-lib-1.1.0 |
