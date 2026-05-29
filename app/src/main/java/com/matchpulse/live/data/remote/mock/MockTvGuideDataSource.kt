package com.matchpulse.live.data.remote.mock

import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvProvider
import com.matchpulse.live.domain.model.TvScheduleItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTvGuideDataSource @Inject constructor() {
    val competitions = listOf(
        TvGuideCompetition("premier-league", "Premier League", "Europe", "England", "Domestic", "Official broadcast guide for selected English fixtures."),
        TvGuideCompetition("uefa-champions-league", "UEFA Champions League", "Europe", "Europe", "International", "Official provider directory for European match nights."),
        TvGuideCompetition("caf-champions-league", "CAF Champions League", "Africa", "Africa", "Africa", "Regional football schedule and legal provider placeholders."),
        TvGuideCompetition("world-cup", "World Cup", "International", "Global", "International", "Global official broadcaster guide placeholders."),
        TvGuideCompetition("afc-champions-league", "AFC Champions League", "Asia", "Asia", "Asia", "Asian competition TV schedule placeholders."),
        TvGuideCompetition("mls", "MLS", "North America", "United States", "Americas", "North American provider guide placeholders."),
    )

    fun schedule(competitionId: String, now: Long = System.currentTimeMillis()): List<TvScheduleItem> {
        val providers = listOf(
            TvProvider(
                id = "official-provider",
                name = "Official Provider",
                type = "Broadcaster",
                url = null,
                isConfigured = false,
                disclaimer = "Provider links will be connected only after legal source configuration.",
            ),
            TvProvider(
                id = "league-site",
                name = "Competition Website",
                type = "Official directory",
                url = null,
                isConfigured = false,
                disclaimer = "Open links only when configured by official backend data.",
            ),
        )
        return List(6) { index ->
            TvScheduleItem(
                id = "$competitionId-schedule-$index",
                competitionId = competitionId,
                matchTitle = listOf(
                    "London Reds vs Manchester Blues",
                    "Madrid Whites vs Barcelona City",
                    "Milan Stripes vs Turin Eagles",
                    "Nairobi Stars vs Cairo United",
                    "Tokyo Waves vs Sydney Harbour",
                    "Toronto North vs Accra Lions",
                )[index],
                kickoffTimeMillis = now + (index + 1) * 3 * HOUR,
                providers = providers,
            )
        }
    }

    private companion object {
        const val HOUR = 60 * 60_000L
    }
}
