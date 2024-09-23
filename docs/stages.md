To alter the next version stage, use the Gradle property: 

**Via command line:**
```shell
-Psemver.stage=<stage>
```

**In any valid `gradle.properties`:**
```properties
semver.stage=<stage>
```

???+ note
    If no stage is provided, a default of `auto` will be used.

The following are possible values:

| Stage      | Pre-release Label         | Example Tag        | Description                |
|------------|---------------------------|--------------------|----------------------------|
| `dev`      | dev                       | `v1.0.0-dev.1`     | Development stage          |
| `alpha`    | alpha                     | `v1.0.0-alpha.1`   | Alpha stage                |
| `beta`     | beta                      | `v1.0.0-beta.1`    | Beta stage                 |
| `rc`       | rc                        | `v1.0.0-rc.1`      | Release Candidate stage    |
| `snapshot` | SNAPSHOT                  | `v1.0.0-SNAPSHOT`  | Snapshot stage             |
| `final`    | final                     | `v1.0.0-final.1`   | Final stage                |
| `ga`       | ga                        | `v1.0.0-ga.1`      | General Availability stage |
| `release`  | release                   | `v1.0.0-release.1` | Release stage              |
| `stable`   | (none)                    | `v1.0.0`           | Stable stage               |
| `auto`     | (depends on previous tag) | -                  | Based on previous tag      |

### Examples

???+ note "Important Note"
    Since no modifier is provided in these examples, the default modifier
    of `auto` used.

    For how to use with modifiers, consult the [Modifiers with Stages](modifiers-with-stages.md) documentation.

Latest tags: `v1.0.0-rc.1`

| Command                             | Next Version    |
|-------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev`      | 1.0.1-dev.1     |
| `./gradlew -Psemver.stage=alpha`    | 1.0.1-alpha.1   |
| `./gradlew -Psemver.stage=beta`     | 1.0.1-beta.1    |
| `./gradlew -Psemver.stage=rc`       | 1.0.0-rc.2      |
| `./gradlew -Psemver.stage=snapshot` | 1.0.1-SNAPSHOT  |
| `./gradlew -Psemver.stage=final`    | 1.0.1-final.1   |
| `./gradlew -Psemver.stage=ga`       | 1.0.1-ga.1      |
| `./gradlew -Psemver.stage=release`  | 1.0.1-release.1 |
| `./gradlew -Psemver.stage=stable`   | 1.0.0           |
| `./gradlew -Psemver.stage=auto`     | 1.0.0-rc.2      |

Latest tag: `v1.0.0`

| Command                             | Next Version    |
|-------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev`      | 1.0.1-dev.1     |
| `./gradlew -Psemver.stage=alpha`    | 1.0.1-alpha.1   |
| `./gradlew -Psemver.stage=beta`     | 1.0.1-beta.1    |
| `./gradlew -Psemver.stage=rc`       | 1.0.1-rc.1      |
| `./gradlew -Psemver.stage=snapshot` | 1.0.1-SNAPSHOT  |
| `./gradlew -Psemver.stage=final`    | 1.0.1-final.1   |
| `./gradlew -Psemver.stage=ga`       | 1.0.1-ga.1      |
| `./gradlew -Psemver.stage=release`  | 1.0.1-release.1 |
| `./gradlew -Psemver.stage=stable`   | 1.0.1           |
| `./gradlew -Psemver.stage=auto`     | 1.0.1           |

