package com.matchpulse.live.data.remote.mock

import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.MatchEvent
import com.matchpulse.live.domain.model.MatchEventType
import com.matchpulse.live.domain.model.MatchStats
import com.matchpulse.live.domain.model.MatchStatus
import com.matchpulse.live.domain.model.Team
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class MockFootballDataSource @Inject constructor() {
    private val teams = listOf(
        Team("london-reds", "London Reds", "LON"),
        Team("manchester-blues", "Manchester Blues", "MAB"),
        Team("madrid-whites", "Madrid Whites", "MAD"),
        Team("barcelona-city", "Barcelona City", "BAR"),
        Team("milan-stripes", "Milan Stripes", "MIL"),
        Team("turin-eagles", "Turin Eagles", "TUR"),
        Team("munich-royals", "Munich Royals", "MUN"),
        Team("paris-stars", "Paris Stars", "PAR"),
        Team("nairobi-stars", "Nairobi Stars", "NAI"),
        Team("cairo-united", "Cairo United", "CAI"),
        Team("lagos-city", "Lagos City", "LAG"),
        Team("accra-lions", "Accra Lions", "ACC"),
        Team("tokyo-waves", "Tokyo Waves", "TOK"),
        Team("sydney-harbour", "Sydney Harbour", "SYD"),
        Team("toronto-north", "Toronto North", "TOR"),
    )

    val competitions = listOf(
        Competition("premier-league", "Premier League", "Europe", "England", "Domestic", "Top-flight English football fixtures and scores."),
        Competition("la-liga", "La Liga", "Europe", "Spain", "Domestic", "Spanish league coverage with legal TV guide support."),
        Competition("serie-a", "Serie A", "Europe", "Italy", "Domestic", "Italian football schedules and score tracking."),
        Competition("bundesliga", "Bundesliga", "Europe", "Germany", "Domestic", "German football fixtures and live match cards."),
        Competition("uefa-champions-league", "UEFA Champions League", "Europe", "Europe", "International", "European continental match nights."),
        Competition("world-cup", "World Cup", "International", "Global", "International", "Global tournament fixtures and official broadcaster guidance."),
        Competition("caf-champions-league", "CAF Champions League", "Africa", "Africa", "Africa", "African club competition schedule and scores."),
        Competition("mls", "MLS", "North America", "United States", "Americas", "North American league fixtures."),
        Competition("afc-champions-league", "AFC Champions League", "Asia", "Asia", "Asia", "Asian continental competition coverage."),
    )

    fun liveMatches(now: Long = System.currentTimeMillis()): List<FootballMatch> =
        generateMatches("live", MatchStatus.LIVE, now - 35 * MINUTE, count = 5)

    fun todayMatches(now: Long = System.currentTimeMillis()): List<FootballMatch> =
        liveMatches(now) + generateMatches("today", MatchStatus.SCHEDULED, now + 90 * MINUTE, count = 5)

    fun upcomingMatches(now: Long = System.currentTimeMillis()): List<FootballMatch> =
        generateMatches("upcoming", MatchStatus.SCHEDULED, now + 8 * HOUR, count = 10)

    fun finishedMatches(now: Long = System.currentTimeMillis()): List<FootballMatch> =
        generateMatches("finished", MatchStatus.FINISHED, now - 3 * HOUR, count = 8)

    fun matchDetail(matchId: String): FootballMatch {
        val all = liveMatches() + todayMatches() + upcomingMatches() + finishedMatches()
        return all.firstOrNull { it.id == matchId } ?: all.first().copy(id = matchId)
    }

    private fun generateMatches(bucket: String, status: MatchStatus, start: Long, count: Int): List<FootballMatch> =
        List(count) { index ->
            val home = teams[(index * 2) % teams.size]
            val away = teams[(index * 2 + 1) % teams.size]
            val competition = competitions[index % competitions.size]
            val live = status == MatchStatus.LIVE
            val finished = status == MatchStatus.FINISHED
            val seed = abs((bucket + index).hashCode())
            FootballMatch(
                id = "$bucket-$index",
                competition = competition,
                homeTeam = home,
                awayTeam = away,
                status = status,
                kickoffTimeMillis = start + index * 45 * MINUTE,
                minute = when {
                    live -> 12 + (seed % 78)
                    finished -> 90
                    else -> null
                },
                homeScore = if (live || finished) seed % 4 else null,
                awayScore = if (live || finished) (seed / 4) % 3 else null,
                venue = listOf("Pulse Arena", "Metro Stadium", "City Ground", "National Sports Park")[index % 4],
                events = sampleEvents(home.name, away.name, live, finished),
                stats = MatchStats(
                    homePossession = 48 + seed % 10,
                    awayPossession = 52 - seed % 10,
                    homeShots = 7 + seed % 9,
                    awayShots = 5 + seed % 7,
                    homeShotsOnTarget = 2 + seed % 5,
                    awayShotsOnTarget = 1 + seed % 4,
                    homeCorners = 2 + seed % 6,
                    awayCorners = 1 + seed % 5,
                    homeFouls = 6 + seed % 8,
                    awayFouls = 7 + seed % 8,
                )
            )
        }

    private fun sampleEvents(home: String, away: String, live: Boolean, finished: Boolean): List<MatchEvent> {
        if (!live && !finished) return emptyList()
        return listOf(
            MatchEvent("e1", 18, MatchEventType.GOAL, home, "Forward 9", "Low finish from inside the box"),
            MatchEvent("e2", 36, MatchEventType.YELLOW_CARD, away, "Midfielder 6", "Late challenge"),
            MatchEvent("e3", 45, MatchEventType.HALF_TIME, "", "", "Half-time"),
        ) + if (finished) {
            listOf(MatchEvent("e4", 90, MatchEventType.FULL_TIME, "", "", "Full-time"))
        } else {
            emptyList()
        }
    }

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
    }
}
