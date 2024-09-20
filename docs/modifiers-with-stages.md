# Modifiers with Stages

To alter the next version incrementation and stage, use the Gradle
properties together:

**Via command line:**

```shell
-Psemver.modifier=<modifier> -Psemver.stage=<stage>
```

**In any valid `gradle.properties`:**

```properties
semver.modifier=<modifier>
semver.stage=<stage>
```

Latest tag: `v1.0.0-rc.1`

| Command                                                     | Next Version    |
|-------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=major` | 2.0.0-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=major` | 2.0.0-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=major` | 2.0.0-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=major` | 2.0.0-rc.1      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=major` | 2.0.0-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=major` | 2.0.0-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=major` | 2.0.0-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=major` | 2.0.0-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=major` | 2.0.0           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=major` | 2.0.0-rc.1      |

Latest tag: `v1.0.0-rc.1`

| Command                                                     | Next Version    |
|-------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=minor` | 1.1.0-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=minor` | 1.1.0-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=minor` | 1.1.0-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=minor` | 1.1.0-rc.1      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=minor` | 1.1.0-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=minor` | 1.1.0-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=minor` | 1.1.0-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=minor` | 1.1.0-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=minor` | 1.1.0           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=minor` | 1.1.0-rc.1      |

Latest tag: `v1.0.0-rc.1`

| Command                                                     | Next Version    |
|-------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=patch` | 1.0.1-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=patch` | 1.0.1-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=patch` | 1.0.1-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=patch` | 1.0.1-rc.1      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=patch` | 1.0.1-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=patch` | 1.0.1-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=patch` | 1.0.1-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=patch` | 1.0.1-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=patch` | 1.0.1           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=patch` | 1.0.1-rc.1      |

Latest tag: `v1.0.0-rc.1`

| Command                                                    | Next Version    |
|------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=auto` | 1.0.1-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=auto` | 1.0.1-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=auto` | 1.0.1-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=auto` | 1.0.0-rc.2      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=auto` | 1.0.1-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=auto` | 1.0.1-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=auto` | 1.0.1-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=auto` | 1.0.1-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=auto` | 1.0.1           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=auto` | 1.0.0-rc.2      |

Latest tag: `v1.0.0`

| Command                                                     | Next Version    |
|-------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=major` | 2.0.0-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=major` | 2.0.0-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=major` | 2.0.0-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=major` | 2.0.0-rc.1      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=major` | 2.0.0-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=major` | 2.0.0-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=major` | 2.0.0-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=major` | 2.0.0-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=major` | 2.0.0           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=major` | 2.0.0           |

Latest tag: `v1.0.0`

| Command                                                     | Next Version    |
|-------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=minor` | 1.1.0-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=minor` | 1.1.0-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=minor` | 1.1.0-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=minor` | 1.1.0-rc.1      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=minor` | 1.1.0-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=minor` | 1.1.0-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=minor` | 1.1.0-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=minor` | 1.1.0-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=minor` | 1.1.0           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=minor` | 1.1.0           |

Latest tag: `v1.0.0`

| Command                                                     | Next Version    |
|-------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=patch` | 1.0.1-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=patch` | 1.0.1-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=patch` | 1.0.1-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=patch` | 1.0.1-rc.1      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=patch` | 1.0.1-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=patch` | 1.0.1-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=patch` | 1.0.1-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=patch` | 1.0.1-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=patch` | 1.0.1           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=patch` | 1.0.1           |

Latest tag: `v1.0.0`

| Command                                                    | Next Version    |
|------------------------------------------------------------|-----------------|
| `./gradlew -Psemver.stage=dev      -Psemver.modifier=auto` | 1.0.1-dev.1     |
| `./gradlew -Psemver.stage=alpha    -Psemver.modifier=auto` | 1.0.1-alpha.1   |
| `./gradlew -Psemver.stage=beta     -Psemver.modifier=auto` | 1.0.1-beta.1    |
| `./gradlew -Psemver.stage=rc       -Psemver.modifier=auto` | 1.0.0-rc.2      |
| `./gradlew -Psemver.stage=snapshot -Psemver.modifier=auto` | 1.0.1-SNAPSHOT  |
| `./gradlew -Psemver.stage=final    -Psemver.modifier=auto` | 1.0.1-final.1   |
| `./gradlew -Psemver.stage=ga       -Psemver.modifier=auto` | 1.0.1-ga.1      |
| `./gradlew -Psemver.stage=release  -Psemver.modifier=auto` | 1.0.1-release.1 |
| `./gradlew -Psemver.stage=stable   -Psemver.modifier=auto` | 1.0.1           |
| `./gradlew -Psemver.stage=auto     -Psemver.modifier=auto` | 1.0.1           |

