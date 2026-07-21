# Configuration cache

The plugin is compatible with Gradle's [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html).

Version calculation is deferred until it is needed (task execution / end-of-build
flow), so **new commits that change the version string do not invalidate the
configuration cache**. On a cache hit, the build still resolves a fresh version
from git when writing `build/semver/semver.properties` and when `project.version`
is stringified during execution.

## Using the version lazily

Prefer the providers on the `semver` extension for task inputs:

```kotlin
tasks.register("printSemver") {
    val version = semver.version
    inputs.property("version", version)
    doLast {
        println(version.get())
    }
}
```

`semver.version` and `semver.versionTag` are lazy. Wire them into task properties
with `Property.set(Provider)` and avoid calling `get()` during configuration.

## Caveat

If build logic (or another plugin) reads `project.version` / `semver.version` /
`semver.versionTag` **during configuration**, Gradle treats that value as a
configuration-cache input. Commits that change the version will then invalidate
the cache. Keep version reads in task actions or other execution-time code when
you want cache reuse across commits.
