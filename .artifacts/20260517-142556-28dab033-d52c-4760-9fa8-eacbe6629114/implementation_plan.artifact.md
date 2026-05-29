# Onboarding Monetization Refinement

Refine onboarding and terms screens to include a premium integrated bottom banner and a single interstitial opportunity upon completion.

## User Review Required

- **Interstitial Placement**: The interstitial will trigger *only* when the user taps "Get Started" on the final onboarding page, before navigating to the Region selection screen. This is a one-time event for new users.

## Proposed Changes

### Core Ad Components

#### [AdPlacement.kt](file:///C:/Users/NGETICH/MatchPulse/MatchPulseLive/app/src/main/java/com/matchpulse/live/core/ads/AdPlacement.kt)

-   Add `ONBOARDING` and `TERMS_GATE` enum values to track these new placements.

### Main Activity / Navigation

#### [MainActivity.kt](file:///C:/Users/NGETICH/MatchPulse/MatchPulseLive/app/src/main/java/com/matchpulse/live/MainActivity.kt)

-   **OnboardingScreen**:
    -   Update layout to include a `PremiumBannerContainer` at the bottom.
    -   Ensure the banner persists across pager swipes by placing it outside the `HorizontalPager`.
    -   Refactor "Get Started" click logic to attempt showing an interstitial before navigation.
-   **TermsGateScreen**:
    -   Add a `PremiumBannerContainer` at the bottom, below the "Accept & Continue" button.
-   **RegionScreen**:
    -   Add a `PremiumBannerContainer` at the bottom for consistency.

## Verification Plan

### Automated Tests
-   `./gradlew assembleDebug`: Ensure project builds successfully.

### Manual Verification
-   **Banner Persistence**: Verify the banner stays visible and stable while swiping through onboarding pages.
-   **Interstitial Trigger**: Complete onboarding and verify an interstitial appears (if available) *exactly once* before reaching the Region screen.
-   **Navigation Safety**: Ensure the app navigates immediately to the Region screen if an interstitial is unavailable or dismissed.
-   **Terms Screen UI**: Verify the banner on the Terms screen doesn't obstruct the checkbox or accept button.
-   **Layout Consistency**: Check ad margins and alignment on various screen sizes to ensure a premium feel.
