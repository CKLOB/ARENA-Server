package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AppleTokenResponse(
    @JsonProperty("id_token") val idToken: String,
)
