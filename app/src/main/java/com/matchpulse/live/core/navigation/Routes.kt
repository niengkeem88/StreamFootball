package com.matchpulse.live.core.navigation

object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Settings = "settings"
    const val About = "about"
    const val Privacy = "privacy"
    const val TermsPage = "terms-page"
}

data class BottomTab(val route: String, val label: String)

val bottomTabs = listOf(
    BottomTab(Routes.Home, "Home"),
    BottomTab(Routes.Settings, "Settings"),
)
