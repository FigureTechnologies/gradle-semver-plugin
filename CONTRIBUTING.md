# Contributing

👋 Thanks for wanting to contribute!
We are always looking for bug fixes, changes, and new features.
If you'd like to work on something please consider making an issue to help track what is being worked on, but someone from the Figure team will be around to help review code and approve pull requests!

## Working Locally

To build locally:

```
./gradlew build
```

To publish locally we recommend commenting out the `signing` plugin, located in the `build-logic/build-conventions/src/main/kotlin/local.publishing.gradle.kts`.
This stops the signing from happening on a publish locally so you don't need to mess around with keys.
Then you can run:

```
./gradlew publishToMavenLocal
```
