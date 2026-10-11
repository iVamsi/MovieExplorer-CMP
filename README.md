# MovieExplorer-CMP

MovieExplorer for Android and iOS. Browse movies, TV series, and actors, search, and keep favorites, a watchlist, and your own collections. One Kotlin codebase with a shared Compose Multiplatform UI. Data comes from [The Movie Database (TMDB)](https://www.themoviedb.org/).

## Setup

Get a free TMDB API key at [themoviedb.org](https://www.themoviedb.org/signup) (Settings → API), then add it to `local.properties` in the project root:

```properties
TMDB_API_KEY=your_api_key_here
TMDB_BASE_URL=https://api.themoviedb.org/3/
```

The build reads these values and generates `AppConfig` for both platforms. The key can also come from the `TMDB_API_KEY` environment variable.

## Run

- **Android:** `./gradlew :androidApp:installDebug` with an emulator or device connected (min SDK 24).
- **iOS:** open `iosApp/iosApp.xcodeproj` in Xcode, pick an iPhone simulator, and press Run. Xcode builds the Kotlin framework through Gradle.

## Attribution

This application uses TMDB and the TMDB APIs but is not endorsed, certified, or otherwise approved by TMDB.

## Quality checks

```bash
./gradlew detekt testAndroidHostTest iosSimulatorArm64Test koverVerify
./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
```

## Known gaps

- Wearable, widget, casting, notification, and release-reminder integrations are outside the first release.
- Trailers open in the browser, and artwork does not derive a backdrop accent color.
- Language changes on iOS open the system Settings page.
- UI instrumentation coverage remains a follow-up; ViewModels, repositories, routing, and lock policy have host tests.
- Category paging currently shares cached movie rows, and paging requests do not yet partition cache keys by language or region.

## License

[MIT](LICENSE)
