# Testing Checklist

- Fresh install routes Splash -> Onboarding -> Region -> Terms -> Main App.
- Returning install routes Splash -> Main App.
- Dark mode toggle applies immediately and persists after restart.
- Mock mode loads Home, Scores, Match Detail, TV Guide, TV Schedule, and Favorites.
- Favorite match IDs persist and update cards immediately.
- Provider buttons show a safe snackbar when links are not configured.
- Diagnostics screen appears only in debug/non-production.
- Ads do not appear on onboarding, region selection, terms, privacy, or settings.
- Interstitials continue navigation if blocked, unloaded, disabled, or failed.
