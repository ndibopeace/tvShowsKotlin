# Watchline for Android

Watchline is a native Android app written in Kotlin and Jetpack Compose. It lets you find TV shows, browse show and episode details, and keep a local record of what you want to watch, are watching, have completed, or have dropped.

## Build

Open this folder in Android Studio, or run:

```bash
./gradlew assembleDebug
```

The project uses Android SDK 35 and supports Android 8.0 (API 26) and newer. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Data and privacy

- Show information, episode guides, schedules, and artwork come from [TVMaze](https://www.tvmaze.com/api). The popular and audience-rating collections use TVMaze's public show listing, which includes its rating and vote-count information.
- The “Trending today” collection is based on shows with episodes on TVMaze’s streaming schedule for the current date; it is not an audience-viewership ranking.
- Watch statuses and watched-episode checkmarks are stored in a SQLite database on the device. Watchline does not create an account or send this personal tracking data to a server.
- The app needs an internet connection to search and load current catalogue information and artwork.
