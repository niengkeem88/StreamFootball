package com.matchpulse.live.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * API-Football returns Map fields (like errors or parameters) as an empty array [] 
 * when there is no data, but as a Map {} when data exists. 
 * This custom serializer handles both.
 */
object FootballMapSerializer : KSerializer<Map<String, String>> {
    private val delegateSerializer = MapSerializer(String.serializer(), String.serializer())
    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun deserialize(decoder: Decoder): Map<String, String> {
        val input = decoder as? JsonDecoder ?: return emptyMap()
        val element = input.decodeJsonElement()
        return if (element is JsonObject) {
            input.json.decodeFromJsonElement(delegateSerializer, element)
        } else {
            emptyMap()
        }
    }

    override fun serialize(encoder: Encoder, value: Map<String, String>) {
        delegateSerializer.serialize(encoder, value)
    }
}

@Serializable
data class FootballFixturesResponse(
    @SerialName("get") val endpoint: String,
    @Serializable(with = FootballMapSerializer::class)
    @SerialName("parameters") val parameters: Map<String, String> = emptyMap(),
    @Serializable(with = FootballMapSerializer::class)
    @SerialName("errors") val errors: Map<String, String> = emptyMap(),
    @SerialName("results") val results: Int,
    @SerialName("response") val response: List<FootballFixtureDto>
)

@Serializable
data class FootballFixtureDto(
    @SerialName("fixture") val fixture: FootballFixtureInfo,
    @SerialName("league") val league: FootballLeagueInfo,
    @SerialName("teams") val teams: FootballTeamsInfo,
    @SerialName("goals") val goals: FootballGoalsInfo,
    @SerialName("status") val status: FootballStatusInfo? = null
)

@Serializable
data class FootballFixtureInfo(
    @SerialName("id") val id: Int,
    @SerialName("referee") val referee: String? = null,
    @SerialName("timezone") val timezone: String,
    @SerialName("date") val date: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("periods") val periods: FootballPeriods,
    @SerialName("venue") val venue: FootballVenue,
    @SerialName("status") val status: FootballStatusInfo
)

@Serializable
data class FootballPeriods(
    @SerialName("first") val first: Long? = null,
    @SerialName("second") val second: Long? = null
)

@Serializable
data class FootballVenue(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("city") val city: String? = null
)

@Serializable
data class FootballStatusInfo(
    @SerialName("long") val long: String,
    @SerialName("short") val short: String,
    @SerialName("elapsed") val elapsed: Int? = null
)

@Serializable
data class FootballLeagueInfo(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("country") val country: String,
    @SerialName("logo") val logo: String,
    @SerialName("flag") val flag: String? = null,
    @SerialName("season") val season: Int,
    @SerialName("round") val round: String? = null
)

@Serializable
data class FootballTeamsInfo(
    @SerialName("home") val home: FootballTeamDto,
    @SerialName("away") val away: FootballTeamDto
)

@Serializable
data class FootballTeamDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("logo") val logo: String,
    @SerialName("winner") val winner: Boolean? = null
)

@Serializable
data class FootballGoalsInfo(
    @SerialName("home") val home: Int? = null,
    @SerialName("away") val away: Int? = null
)

@Serializable
data class FootballLeaguesResponse(
    @SerialName("get") val endpoint: String,
    @Serializable(with = FootballMapSerializer::class)
    @SerialName("parameters") val parameters: Map<String, String> = emptyMap(),
    @Serializable(with = FootballMapSerializer::class)
    @SerialName("errors") val errors: Map<String, String> = emptyMap(),
    @SerialName("results") val results: Int = 0,
    @SerialName("response") val response: List<FootballLeagueDetailDto>
)

@Serializable
data class FootballLeagueDetailDto(
    @SerialName("league") val league: FootballLeagueBase,
    @SerialName("country") val FootballCountryInfo: FootballCountryInfo
)

@Serializable
data class FootballLeagueBase(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("logo") val logo: String
)

@Serializable
data class FootballCountryInfo(
    @SerialName("name") val name: String,
    @SerialName("code") val code: String? = null,
    @SerialName("flag") val flag: String? = null
)
