# MatchPulse Live QA Report

## Build Verification

Validated from a clean project state on Windows with JDK 17 and Android SDK 36.

| Command | Result | Notes |
| --- | --- | --- |
| `.\gradlew.bat clean --no-daemon --stacktrace` | Passed | Cleaned build outputs successfully. |
| `.\gradlew.bat assembleDebug --no-daemon --stacktrace` | Passed | Debug APK generated. |
| `.\gradlew.bat bundleDebug --no-daemon --stacktrace` | Passed | Debug bundle generated. |
| `.\gradlew.bat assembleRelease --no-daemon --stacktrace` | Passed | Release APK generated with current release config. |
| `.\gradlew.bat bundleRelease --no-daemon --stacktrace` | Passed | Release AAB generated and signed with default Gradle release behavior. |
| `.\gradlew.bat lint --no-daemon --stacktrace` | Passed | Blocking property escaping lint error fixed. Remaining version-availability warnings are non-blocking. |
| `.\gradlew.bat test --no-daemon --stacktrace` | Passed | Unit tests pass for config, status mapping, cache policy, ad frequency policy, network error mapping, and provider URL validation. |

`connectedAndroidTest` was not run because `adb.exe` was not available under the expected SDK `platform-tools` path on this workstation.

## Fixes Applied

- Fixed `android.aapt2FromMavenOverride` property escaping so lint passes.
- Added Android 12+ data extraction rules and backup rules, excluding Room cache files from backup/transfer.
- Added monochrome launcher icon metadata for themed Android launchers.
- Fixed a first-launch/returning-user race by waiting for DataStore settings to load before leaving Splash.
- Reset interstitial session count and major-navigation count when a new app session starts.
- Added explicit production guard for the diagnostics route.
- Added AdView disposal in Compose to avoid banner lifecycle leaks.
- Replaced `Uri.parse(...)` calls with KTX `toUri()`.
- Added stable keys to major lazy lists to improve scroll stability.
- Improved TV schedule provider button layout for small devices.
- Increased touch-target resilience for primary buttons, search fields, settings rows, and match cards.
- Replaced deprecated Room destructive migration call with the current overload.
- Silenced Kotlin annotation-target warnings with explicit `@param:` injection qualifiers.

## UI And Dark Mode

- Compose screens were reviewed structurally for spacing, card padding, scroll behavior, touch targets, and bottom-navigation overlap.
- Home, Scores, TV Guide, TV Schedule, Match Detail, Favorites, and Settings all use Material 3 colors and app theme colors.
- Dark mode is controlled by DataStore-backed settings and applies immediately through the root theme.
- Provider action buttons are now full width in schedule cards, reducing overflow risk on small phones.
- Lazy lists now use stable item keys for smoother scrolling and better state preservation.

Manual visual QA on physical devices/emulators is still recommended before open testing, especially for screenshots, landscape behavior, and tablet polish.

## Navigation

- First launch flow remains Splash -> Onboarding -> Region -> Terms -> Main App.
- Returning-user routing now waits for persisted DataStore state before deciding the next screen.
- Bottom navigation keeps tab routes single-top and state-restored where practical.
- Developer diagnostics are hidden from Settings in production-like builds and route-gated if reached internally.

## Network And Cache

- Mock mode remains the default and works without a backend.
- Backend mode routes through the configured `API_BASE_URL`; the Android app does not call API-Football directly.
- Retrofit exceptions are mapped to user-safe messages.
- Room cache supports stale-data display while refresh attempts run.
- API diagnostics reports backend health, provider health, data calls, last success, and last user-safe API error.

Backend/offline behavior still needs device testing against the real production backend and controlled network conditions.

## AdMob Verification

- Debug/development builds use Google test ad unit IDs when ads are enabled.
- Production ads require Gradle-provided AdMob app/banner/interstitial/native IDs.
- Production ads disable safely if required IDs are missing.
- Banner placements remain limited to Home, Scores, TV Guide, TV Schedule Detail, Match Detail lower content, and Favorites lower content.
- No ads are placed on onboarding, region selection, terms, privacy, or settings.
- Interstitials are transition-based and continue navigation if disabled, capped, unloaded, or failed.
- Frequency policy covers first 90 seconds, 3-minute cooldown, 3 per session, 6 per day, and 4 major navigations.
- Session interstitial counts now reset on app open.

## Play Policy Review

- No unauthorized livestream feature exists.
- No WebView streaming, IPTV/M3U scraping, user-submitted stream links, or embedded unauthorized video player exists.
- No API-Football key or backend provider secret exists in Android source.
- Provider URLs must be backend-configured, `https`, and enabled before opening.
- Legal disclaimers are present in the TV Guide, schedule flow, about/privacy/terms copy, and docs.
- Permissions are limited to `INTERNET`, `ACCESS_NETWORK_STATE`, and AdMob `AD_ID`.

## Known Issues And Recommendations

- Dependency version lint warnings remain informational; update libraries in a separate dependency refresh pass after device smoke tests.
- Release signing currently uses the default Gradle release behavior; configure a Play upload key before Play Console production submission.
- `connectedAndroidTest` requires a working Android device/emulator and SDK platform-tools.
- Add final screenshots, feature graphic, hosted privacy policy URL, and real backend/AdMob production properties before closed/open testing.
- Run manual QA on small phones, tall phones, tablets, dark mode, slow network, offline, and repeated ad-trigger navigation.
