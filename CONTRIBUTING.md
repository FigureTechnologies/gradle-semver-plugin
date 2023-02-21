# Contributing

👋 Thanks for wanting to contribute! 
We are always looking for bug fixes, changes, and new features. 
If you'd like to work on something please consider making an issue to help track what is being worked on, but someone from the Figure team will be around to help review code and approve pull requests!

## Working Locally

To build locally:

```
./gradlew clean build
```

To publish locally we recommend commenting out the `signing` plugin, located in the `build-logic/build-conventions/src/main/kotlin/local.publishing.gradle.kts`.
This stops the signing from happening on a publish locally so you don't need to mess around with keys.
Then you can run:

```
./gradlew publishToMavenLocal
```

## Tests

Currently we don't have a lot of tests, but we do have one set up that uses the `GradleRunner` to create a gradle environment just for the test that we can then apply our code to and run tasks.
Check out the `BuildLogicFunctionalSpec` to see how this works.

## Licensing

One thing we need to ensure is that we maintain a license header on each source file.
We have automated this through the use of a gradle plugin!
Whenever a `.gradlew build` is run, the license headers will automatically be regenerated. Please make sure to commit these updates.

More info about this plugin is available on github: https://github.com/CadixDev/licenser
