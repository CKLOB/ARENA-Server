package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoProfile(
    @JsonProperty("profile_image_url") val imageUrl: String?,
)
