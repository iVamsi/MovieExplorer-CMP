# MovieExplorer-CMP — project notes for AI agents

Compose Multiplatform port of the Android MovieExplorer app. Targets Android and iOS only.

## Stack

Kotlin 2.4.20, AGP 9 (built-in Kotlin), Compose Multiplatform 1.12.1, Material 3 1.12.0-alpha03, Koin 4.2.2, Ktor 3.6.0 (OkHttp on Android, Darwin on iOS), Room 2.8.5 KMP with bundled SQLite, DataStore 1.2.1, Paging 3.5.2, Coil 3.6.3, Navigation Compose 2.10.0-rc01, kotlinx-datetime, Kermit. Versions live only in `gradle/libs.versions.toml`. Plugins are applied through `build-logic/convention`; never add plugin aliases or versions in module build files.

## Layout

- `androidApp/` — Android application: `MainActivity` and `Application` only.
- `iosApp/` — Xcode project; SwiftUI shell hosting Compose (`import ComposeApp`).
- `composeApp/` — `App()`, navigation, Koin root, deep links. Builds the iOS framework `ComposeApp`.
- `core/{common,model,network,database,domain,testing,ui}`
- `data/{actors,movies,series,user}`
- `feature/{actors,collections,movies,profile,search,series,settings}`

Features never import `com.movieapp.data..`. Repository interfaces live in `com.movieapp.core.domain.repository`.

## Rules

- No `android.*`, `java.*`, or `javax.*` imports in `commonMain`. Platform code goes in `androidMain` / `iosMain` behind `expect`/`actual` or an interface bound in a Koin platform module.
- Packages match the original app (`com.movieapp.feature.movies...`); only the source root differs.
- UDF: `UiState` data class, `UiEvent` sealed interface with a single `onEvent`, side effects through a `Channel`.
- Use `com.movieapp.core.common.Result`, inject `kotlin.time.Clock` and `AppDispatchers`, `fun interface` for single-method interfaces.
- Route IDs reach detail ViewModels as constructor parameters through Koin `parametersOf`.
- Strings are Compose resources (`Res.string.x`) with positional format args (`%1$s`).
- Test names use backticks: `` `method - condition - expected` ``.
- No `TODO` comments.

## Verify

```bash
./gradlew detekt testAndroidHostTest koverVerify
./gradlew iosSimulatorArm64Test
./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```
