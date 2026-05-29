# Release Checklist

- Confirm `APP_ENV=production`.
- Confirm `FOOTBALL_API_MODE=backend`.
- Confirm `API_BASE_URL` is HTTPS and not localhost.
- Confirm no API-Football key exists in Android source or Gradle properties.
- Confirm AdMob production app ID and ad unit IDs are passed at build time.
- Confirm legal TV provider URLs are backend-controlled and official.
- Confirm diagnostics screen is hidden in production.
- Build and upload the release AAB through Play App Signing.
- Complete Google Play data safety and ads declarations.
