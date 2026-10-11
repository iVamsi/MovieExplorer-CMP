# MovieExplorer-CMP

MovieExplorer for Android and iOS. Browse movies, TV series, and actors, search, and keep favorites, a watchlist, and your own collections. One Kotlin codebase with a shared Compose Multiplatform UI. Data comes from [The Movie Database (TMDB)](https://www.themoviedb.org/).

This is a port of the Android-only [MovieExplorer](https://github.com/iVamsi/MovieExplorer).

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

## Known issues

Carried over unchanged from the Android app so both behave the same:

- The non-paged movie rows fall back to any fresh cached movie, not only movies from the same category.
- The paged `RemoteMediator`s ignore the user's language and region settings.

## Attribution

This application uses TMDB and the TMDB APIs but is not endorsed, certified, or otherwise approved by TMDB.

## License

[MIT](LICENSE)
