# Override Version

To specify the exact next version, use the Gradle property:

**Via command line:**

```shell
-Psemver.overrideVersion=<overrideVersion>
```

**In any valid `gradle.properties`:**

```properties
semver.overrideVersion=<overrideVersion>
```

### Examples

Latest tag: `v2.0.0`

| Command                                    | Next Version |
|--------------------------------------------|--------------|
| `./gradlew -Psemver.overrideVersion=1.5.9` | v1.5.9       |


Latest tag: `v2.0.0-rc.1`

| Command                                                     | Next Version            |
|-------------------------------------------------------------|-------------------------|
| `./gradlew -Psemver.overrideVersion=1.5.9-my-test-hotfix.1` | v1.5.9-my-test-hotfix.1 |
