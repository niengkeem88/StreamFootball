package com.matchpulse.live.core.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Region = "region"
    const val Terms = "terms"
    const val Home = "home"
    const val Scores = "scores"
    const val TvGuide = "tv-guide"
    const val Favorites = "favorites"
    const val Settings = "settings"
    const val MatchDetail = "match/{matchId}"
    const val TvSchedule = "tv-schedule/{competitionId}"
    const val About = "about"
    const val Privacy = "privacy"
    const val TermsPage = "terms-page"
    const val Diagnostics = "diagnostics"
    const val Maintenance = "maintenance"
    const val Update = "update"

    fun match(matchId: String) = "match/$matchId"
    fun tvSchedule(competitionId: String) = "tv-schedule/$competitionId"
}

data class BottomTab(val route: String, val label: String)

val bottomTabs = listOf(
    BottomTab(Routes.Home, "Home"),
    BottomTab(Routes.Scores, "Scores"),
    BottomTab(Routes.TvGuide, "TV Guide"),
    BottomTab(Routes.Favorites, "Favorites"),
    BottomTab(Routes.Settings, "Settings"),
)
