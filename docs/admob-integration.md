# AdMob Debug Ad Integration

## Overview
MatchPulse Live includes a robust AdMob integration layer designed for safe development and policy-compliant production behavior.

## Debug Mode
In debug builds (`BuildConfig.DEBUG == true`), the app **always** uses official Google Test IDs. This prevents accidental production ad clicks during development.

### Official Test IDs Used:
- **Banner**: `ca-app-pub-3940256099942544/6300978111`
- **Interstitial**: `ca-app-pub-3940256099942544/1033173712`
- **Native**: `ca-app-pub-3940256099942544/2247696110`
- **Rewarded**: `ca-app-pub-3940256099942544/5224354917`

## Frequency Caps
Interstitial ads are strictly controlled to ensure a good user experience and policy compliance:
- **First Session Delay**: No interstitials within the first 90 seconds of app launch.
- **Minimum Gap**: At least 3 minutes between interstitials.
- **Session Cap**: Maximum 3 interstitials per app session.
- **Daily Cap**: Maximum 6 interstitials per day.
- **Navigation Gap**: At least 4 major navigation events (tab switches) between interstitials.

## Integration Points
- **Banners**: Home (dashboard), Scores (every 6 matches), TV Guide (after filters), Match Detail (bottom), and Favorites (bottom).
- **Interstitials**: Triggered when opening a TV Schedule detail page or clicking an external official provider link.
- **Diagnostics**: A developer-only screen provides real-time status of the ad system, masked IDs, and buttons to test ads or reset frequency caps.

## Production Configuration
To use production ads in release builds, provide the following Gradle properties:
- `ADMOB_ANDROID_APP_ID`
- `ADMOB_ANDROID_BANNER_ID`
- `ADMOB_ANDROID_INTERSTITIAL_ID`
- `ADMOB_ANDROID_NATIVE_ID`
- `ADMOB_ANDROID_REWARDED_ID`
- `ENABLE_ADS=true`

If these properties are missing in a release build, ads will be automatically disabled to prevent crashes.

## Testing on Real Devices
To test ads on a real device without using production ads, find your device's AdMob ID in logcat (search for "Use RequestConfiguration.Builder().setTestDeviceIds") and add it to `ADMOB_TEST_DEVICE_IDS` in your `local.properties` or command line:

```powershell
.\gradlew assembleDebug -PADMOB_TEST_DEVICE_IDS="YOUR_DEVICE_ID"
```
