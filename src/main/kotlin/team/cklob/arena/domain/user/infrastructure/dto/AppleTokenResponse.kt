package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppleTokenResponse(
    @JsonProperty("id_token") val idToken: String,
)
