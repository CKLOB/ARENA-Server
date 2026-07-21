package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleUserInfoResponse(
    @JsonProperty("sub") val subject: String,
    val email: String?,
    val picture: String?,
)
