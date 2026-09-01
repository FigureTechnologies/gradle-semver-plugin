# Modifiers

Modifiers control which **core version component** changes when calculating the
next version (`major.minor.patch`).

Use the Gradle property:

**Via command line:**
```shell
-Psemver.modifier=<modifier>
```

**In any valid `gradle.properties`:**
```properties
semver.modifier=<modifier>
```

???+ note
    If no modifier is provided, a default of `auto` will be used.

## Values

| Modifier | Description |
|----------|-------------|
| `major`  | Always increments the major version |
| `minor`  | Always increments the minor version |
| `patch`  | Always increments the patch version |
| `auto`   | Default policy: continue a matching stage-based pre-release when possible; otherwise behave like `patch` |

`stage` chooses the pre-release **label** (`rc`, `beta`, …). `modifier` chooses
how the **numeric** part moves. See [Stages](stages.md) and
[Modifiers with Stages](modifiers-with-stages.md).

???+ note "`auto` is a policy, not a fourth SemVer component"
    `major`, `minor`, and `patch` always bump that core component.

    `auto` is the default when you omit the property (or set it explicitly to
    reset a `gradle.properties` override). It means:

    1. If the latest tag is a **stage-based** pre-release (`rc`, `alpha`, …)
       and you are continuing that same stage (or using `stage=auto`), bump
       only the pre-release counter — for example `1.0.0-rc.1` → `1.0.0-rc.2`.
    2. Otherwise, bump **patch** the same way `modifier=patch` would.

    Branch-based pre-releases (for example `1.0.1-my-feature.1`) are not
    stage-based; on the main branch they do not trigger the continue path.

## `auto` vs `patch`

These differ only while iterating a matching stage-based pre-release.

Latest tag: `v1.0.0-rc.1` (default `stage=auto`)

| Modifier | Next version | Why |
|----------|--------------|-----|
| `auto` (default) | `v1.0.0-rc.2` | Continue the current `rc` line |
| `patch` | `v1.0.1` | Force a patch bump (drops the pre-release when stage is also `auto`) |

With an explicit matching stage:

| Command | Next version |
|---------|--------------|
| `./gradlew -Psemver.stage=rc` (modifier defaults to `auto`) | `v1.0.0-rc.2` |
| `./gradlew -Psemver.stage=rc -Psemver.modifier=patch` | `v1.0.1-rc.1` |

Use **`auto`** (or omit the property) when iterating release candidates or
other staged pre-releases. Use **`patch`** when you intentionally want the next
patch core version, including starting a new `X.Y.Z+1-<stage>.1` line instead
of continuing `X.Y.Z-<stage>.N`.

On a stable latest tag, `auto` and `patch` produce the same result.

### Examples

???+ note "Important Note"
    Since no stage is provided in these examples, the default stage of `auto`
    is used.

    For how to use with stages, consult the [Modifiers with Stages](modifiers-with-stages.md) documentation.

Latest tag: `v1.0.0-rc.1`

| Command                             | Next Version |
|-------------------------------------|--------------|
| `./gradlew -Psemver.modifier=major` | v2.0.0       |
| `./gradlew -Psemver.modifier=minor` | v1.1.0       |
| `./gradlew -Psemver.modifier=patch` | v1.0.1       |
| `./gradlew -Psemver.modifier=auto`  | v1.0.0-rc.2  |

Latest tag: `v1.0.0`

| Command                             | Next Version |
|-------------------------------------|--------------|
| `./gradlew -Psemver.modifier=major` | v2.0.0       |
| `./gradlew -Psemver.modifier=minor` | v1.1.0       |
| `./gradlew -Psemver.modifier=patch` | v1.0.1       |
| `./gradlew -Psemver.modifier=auto`  | v1.0.1       |

Latest tags (sorted by latest first) and on main branch:

- `v1.0.1-my-feature.1`
- `v1.0.0`

???+ info "Important"
    The latest tag is `v1.0.1-my-feature.1`, however, this is a special
    pre-release type that does not affect the calculation of the
    next version when on a main branch given a modifier.

| Command                             | Next Version |
|-------------------------------------|--------------|
| `./gradlew -Psemver.modifier=major` | v2.0.0       |
| `./gradlew -Psemver.modifier=minor` | v1.1.0       |
| `./gradlew -Psemver.modifier=patch` | v1.0.1       |
| `./gradlew -Psemver.modifier=auto`  | v1.0.1       |
