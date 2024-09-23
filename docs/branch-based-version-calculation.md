Branch-based version calculation provides a way to automatically generate unique versions based on the current branch.
This can be good for local testing and integration testing with other libraries
or services for a period of time prior to creating a stable release.

???+ info "Important"
    Every commit gets a new version!

### Examples

Branching from the main branch

| Latest Tag | Current Branch         | Branched From | Commits past main | Next version                 |
|------------|------------------------|---------------|-------------------|------------------------------|
| `v1.0.0`   | `me/sc-123/my-feature` | `main`        | 4                 | 1.0.1-me-sc-123-my-feature.4 |
| `v1.0.0`   | `my-feature`           | `main`        | 12                | 1.0.1-my-feature.12          |
| `v1.0.0`   | `my-sub-feature`       | `my-feature`  | 16                | 1.0.1-my-sub-feature.16      |
| `v1.0.0`   | `main`                 | -             | 7                 | 1.0.1                        |

Branching from the development branch

| Latest Tag         | Current Branch         | Branched From | Commits past develop | Next version                 |
|--------------------|------------------------|---------------|----------------------|------------------------------|
| `v1.0.0`           | `me/sc-123/my-feature` | `develop`     | 4                    | 1.0.1-me-sc-123-my-feature.4 |
| `v1.0.0`           | `my-feature`           | `develop`     | 12                   | 1.0.1-my-feature.12          |
| `v1.0.0`           | `my-sub-feature`       | `my-feature`  | 16                   | 1.0.1-my-sub-feature.16      |
| `v1.0.0-develop.1` | `develop`              | -             | 7                    | 1.0.1-develop.8              |

### Forcing a new version

???+ tip
    Need a new version but don't need to make any changes to your branch?
    Just create an empty commit!

```shell
git commit --allow-empty -m "Empty commit"
```

Alternatively, you can use the `semver.appendBuildMetadata` property to append build metadata to the version.

**Via command line:**

```shell
./gradlew -Psemver.appendBuildMetadata=(always|never|locally)
```

**In any valid `gradle.properties`:**

```properties
semver.appendBuildMetadata=(always|never|locally)
```

