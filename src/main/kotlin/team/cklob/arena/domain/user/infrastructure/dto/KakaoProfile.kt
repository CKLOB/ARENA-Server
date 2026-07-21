package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProfile(
    @JsonProperty("profile_image_url") val imageUrl: String?,
)
