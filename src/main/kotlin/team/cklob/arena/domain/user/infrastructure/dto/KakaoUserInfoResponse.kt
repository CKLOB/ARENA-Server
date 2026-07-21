package team.cklob.arena.domain.user.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    val id: Long,
    @JsonProperty("kakao_account") val account: KakaoAccount?,
)
