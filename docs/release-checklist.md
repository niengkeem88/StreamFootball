# MatchPulse Live Release Checklist

## Build Command

Use this production AAB command when backend and AdMob values are ready:

```powershell
.\gradlew.bat bundleRelease --no-daemon --stacktrace `
  -PAPP_ENV=production `
  -PFOOTBALL_API_MODE=backend `
  -PAPI_BASE_URL=https://your-production-backend.com `
  -PENABLE_ADS=true `
  -PADMOB_ANDROID_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx `
  -PADMOB_ANDROID_BANNER_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx `
  -PADMOB_ANDROID_INTERSTITIAL_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx `
  -PADMOB_ANDROID_NATIVE_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx
```

## Pre-Upload

- Replace all placeholder AdMob production IDs with real AdMob IDs passed by Gradle properties.
- Set `API_BASE_URL` to the production HTTPS backend.
- Confirm `FOOTBALL_API_MODE=backend` for production release candidates.
- Confirm API-Football keys exist only in backend environment variables.
- Configure Play upload signing outside source control.
- Increment `versionCode` for every Play upload.
- Confirm `versionName` is the intended public version.
- Rebuild and archive the final `app-release.aab`.
- Verify no `localhost`, `127.0.0.1`, secrets, or debug-only URLs are present.

## Play Console

- Create or update app listing for package `com.matchpulse.live`.
- Upload release AAB to internal testing first.
- Add phone screenshots, tablet screenshots if targeting tablets, app icon, and feature graphic.
- Host the final privacy policy and paste its URL into Play Console.
- Complete Data Safety form:
  - disclose Google Mobile Ads SDK if ads are enabled
  - disclose device/local preferences only as applicable
  - note no account system in v1
  - note no unauthorized streaming or user-submitted links
- Complete Ads declaration.
- Complete Content Rating questionnaire.
- Provide app access instructions if production backend ever requires auth.
- Configure internal testers and closed testing track.

## Manual QA Before Closed Testing

- Fresh install flow: Splash -> Onboarding -> Region -> Terms -> Main App.
- Returning install flow: Splash -> Main App.
- Region change from Settings.
- Dark mode toggle persists after relaunch.
- Home, Scores, Match Detail, TV Guide, TV Schedule, Favorites, Settings.
- Mock mode with no backend.
- Backend mode with production-like backend.
- Offline mode after cache is populated.
- Empty API response behavior.
- Timeout, 429, 500, and malformed response behavior.
- Banner placements do not overlap content.
- Interstitials obey cooldown and never block required navigation.
- Provider buttons open only configured HTTPS official links.
- Provider placeholders show the safe snackbar.

## Policy Gate

- No unauthorized livestreams.
- No IPTV/M3U scraping.
- No user-submitted stream links.
- No copyrighted club logos or broadcaster logos without rights.
- No "watch free live football" claims.
- No debug diagnostics visible in production settings.
- No dangerous permissions.
