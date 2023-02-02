# Publishing

This plugin is published to the Gradle plugin portal via GitHub Actions.
In order for this to publish, there are two important pieces: signing key, and publishing key.

### Signing
The signing key step is to validate that it is us publishing this artifact.
This is done with our `69C08EA0` GPG key.
The following configuration is needed to sign our artifacts:

```
signing.keyId=69C08EA0
signing.password=
signing.secretKeyRingFile=
```

### Gradle Portal Publish

Our artifact ends up living on the Gradle Plugin Portal, or you may know it as `gradlePluginPortal()`.
To upload to this repository we need to give them the API key information for our portal account.
The following configuration is needed to authenticate with the Gradle Plugin Portal:

```
gradle.publish.key=
gradle.publish.secret=
```
