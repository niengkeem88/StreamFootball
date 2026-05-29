package com.matchpulse.live.data.mapper

import com.matchpulse.live.data.remote.dto.FootballFixtureDto
import com.matchpulse.live.data.remote.dto.FootballLeagueDetailDto
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.MatchStatus
import com.matchpulse.live.domain.model.Team

fun FootballFixtureDto.toDomain(): FootballMatch {
    return FootballMatch(
        id = fixture.id.toString(),
        competition = Competition(
            id = league.id.toString(),
            name = league.name,
            region = league.country,
            country = league.country,
            category = "League",
            description = league.round ?: "",
            logoUrl = league.logo
        ),
        homeTeam = Team(
            id = teams.home.id.toString(),
            name = teams.home.name,
            shortName = teams.home.name.take(3).uppercase(),
            logoUrl = teams.home.logo
        ),
        awayTeam = Team(
            id = teams.away.id.toString(),
            name = teams.away.name,
            shortName = teams.away.name.take(3).uppercase(),
            logoUrl = teams.away.logo
        ),
        status = MatchStatus.fromWire(fixture.status.short),
        kickoffTimeMillis = fixture.timestamp * 1000L,
        minute = if (MatchStatus.fromWire(fixture.status.short) == MatchStatus.LIVE) fixture.status.elapsed else null,
        homeScore = goals.home,
        awayScore = goals.away,
        venue = fixture.venue.name ?: "TBC",
        events = emptyList(), // Detailed events could be mapped later
        stats = null, // Stats could be mapped later
        isStale = false
    )
}

fun FootballLeagueDetailDto.toDomain(): Competition {
    return Competition(
        id = league.id.toString(),
        name = league.name,
        region = FootballCountryInfo.name,
        country = FootballCountryInfo.name,
        category = league.type,
        description = "${FootballCountryInfo.name} - ${league.type}",
        logoUrl = league.logo
    )
}
