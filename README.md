# MatchPulse Live

Native Android football companion app for live scores, fixtures, favorites, a legal TV guide, API diagnostics, and AdMob-ready monetization.

## Stack

- Kotlin, Jetpack Compose, Material 3
- MVVM with repository pattern
- Hilt dependency injection
- Retrofit, OkHttp, Kotlinx Serialization
- Room cache and DataStore Preferences
- Navigation Compose and AndroidX SplashScreen
- Google Mobile Ads SDK
- Coil dependency included for future team/provider image loading

## Policy Position

MatchPulse Live does not host, embed, scrape, distribute, or promote unauthorized football livestreams. TV guide links are legal provider directory links only and must be configured by backend data. Future in-app playback is gated behind licensed-provider metadata and `ENABLE_EXPERIMENTAL_PLAYER=false` by default.

## Debug Mock Mode

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace
```

Mock mode is the default and runs without a backend:

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace `
  -PAPP_ENV=development `
  -PFOOTBALL_API_MODE=mock `
  -PENABLE_ADS=false
```

## Debug Backend Mode

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace `
  -PAPP_ENV=development `
  -PFOOTBALL_API_MODE=backend `
  -PAPI_BASE_URL=https://your-backend-url.com `
  -PENABLE_ADS=true
```

No API-Football key belongs in the Android app. Store football provider keys only in backend environment variables.

## Release AAB

```powershell
.\gradlew.bat bundleRelease --no-daemon --stacktrace `
  -PAPP_ENV=production `
  -PFOOTBALL_API_MODE=backend `
  -PAPI_BASE_URL=https://your-backend-url.com `
  -PENABLE_ADS=true `
  -PADMOB_ANDROID_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx `
  -PADMOB_ANDROID_BANNER_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx `
  -PADMOB_ANDROID_INTERSTITIAL_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx `
  -PADMOB_ANDROID_NATIVE_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx
```

Production ads are disabled safely if required production ad unit IDs are missing.

## Backend Endpoints

The Android app expects these backend-owned endpoints when `FOOTBALL_API_MODE=backend`:

- `GET /health`
- `GET /v1/football/provider/health`
- `GET /v1/football/matches/live`
- `GET /v1/football/matches/today`
- `GET /v1/football/matches/upcoming`
- `GET /v1/football/matches/finished`
- `GET /v1/football/matches/{matchId}`
- `GET /v1/football/competitions`
- `GET /v1/football/competitions/{competitionId}/matches`
- `GET /v1/tv-guide/competitions`
- `GET /v1/tv-guide/competitions/{competitionId}/schedule`
- `GET /v1/app/config`

## AdMob

Debug/development builds use official Google test IDs when ads are enabled. Production builds require real IDs through Gradle properties. Interstitials are capped:

- none in first 90 seconds
- at least 3 minutes apart
- max 3 per session
- max 6 per day
- at least 4 major navigations between interstitials

Ads are not shown on onboarding, region selection, terms, privacy, or first-launch routing.

## Google Play Readiness

Before submitting:

- configure real HTTPS backend URL
- configure production AdMob IDs
- add final privacy policy URL and store listing
- configure Play App Signing/upload key outside source control
- verify release AAB in Play Console internal testing
- confirm official TV provider links are legal and backend-controlled

## Validation

```powershell
.\gradlew.bat test --no-daemon --stacktrace
.\gradlew.bat assembleDebug --no-daemon --stacktrace
.\gradlew.bat bundleRelease --no-daemon --stacktrace
```

## QA And Release Docs

- `docs/qa-report.md`
- `docs/release-checklist.md`
