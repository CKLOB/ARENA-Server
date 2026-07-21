package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AppleJwk(
    val kid: String,
    val kty: String,
    @JsonProperty("n") val modulus: String,
    @JsonProperty("e") val exponent: String,
)
