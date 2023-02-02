This file is a copy/paste from the old `platform-portal` repo for posterity. It should eventually be reviewed/

---

# Semantic Versioning

Official specification and description: https://semver.org/

## Using the Semver Gradle plugin

See full documentation here: https://github.com/FigureTechnologies/gradle-semver-plugin

### TL;DR

The version (and tag name) is calculated at runtime from a combination of current branch & target branch (version). If a `develop` branch is present `Flow` mode will be enabled, if it's absent, `Flat` mode will be used. See the official documentation for the default configurations for each (branch regexes). Here is a [minimal and typical example of usage](https://github.com/FigureTechnologies/libs-exposed-tools/blob/main/build-logic/build-conventions/src/main/kotlin/figure.build-conventions.gradle.kts#L4-L9).
It is important to use the expected branch name, so that a branch regex in the configuration will be matched. 

**Note** the preset configurations for `Flow` and `Flat` both assumes `main` branch, if you're still using `master`, see this [custom configuration as example (Flow mode)](https://github.com/FigureTechnologies/service-risk/blob/360b5a44e98c4b470896160dd19da66e8267e674/build-logic/build-conventions/src/main/kotlin/figure.build-conventions.gradle.kts). This example also adds support for branch names typically used with ShortCut, eg:
* `"""^.+/sc-\d+/.+""".toRegex()`
* `"""^.+/\d+/.+"""`
* `"""^.+/no-ticket/.+"""`

More information on the version calculation is available by enabling info logging, eg running `./gradlew cv -i`.

## Limiting dependencies to release versions 
     
A reasonable approach would be to use Gradle's attribute metadata, however it appears to be [broken](https://github.com/gradle/gradle/issues/20016).

Another, less desirable approach would be to use a ComponentSelection rule, here's one such possibility in the build convention plugin:
                                                                           
```kotlin
val InvalidQualifiers = setOf("alpha", "beta", "rc", "nightly")
val OnlyReleaseArtifacts = setOf("figure-retry", "stream-data")
val WhiteListedMavenGroups = setOf("com.figure", "tech.figure", "io.provenance")

configurations.all {
    resolutionStrategy {
        componentSelection {
            all {
                when {
                    OnlyReleaseArtifacts.any { candidate.moduleIdentifier.name.startsWith(it) } && !safeVersion(candidate.version)?.preRelease.isNullOrEmpty() -> {
                        reject("rejecting prerelease version for OnlyReleaseArtifact[$candidate]")
                    }
                    WhiteListedMavenGroups.none { candidate.group.startsWith(it) } && InvalidQualifiers.any { candidate.version.contains(it) } -> {
                        reject("invalid qualifier versions for $candidate")
                    }
                }
            }
        }
    }
}

fun safeVersion(version: String): SemVer? {
    return try {
        SemVer.parse(version)
    } catch (e: Exception) {
        println("failed to parse $version")
        null
    }
}
```                                                                                                             
       
The `OnlyReleaseArtifacts` should take precedence over any white listed groups to enforce release only semantic versions. For example, in this case, any artifact which artifactID starts with `figure-retry` and has a pre-release version will be rejected until a version without a pre-release label is found. 

Make sure that https://github.com/swiftzer/semver is on the classpath for the buildscript, eg in FSB adding it to `build-logic/build-conventions/build.gradles.kts`:

```kotlin
dependencies {
    implementation(tpLibs.swiftzer.semver)
}
```
