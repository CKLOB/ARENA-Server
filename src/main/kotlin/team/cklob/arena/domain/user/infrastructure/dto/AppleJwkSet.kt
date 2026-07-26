package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppleJwkSet(val keys: List<AppleJwk>)
