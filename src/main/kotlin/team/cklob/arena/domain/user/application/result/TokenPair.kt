package team.cklob.arena.domain.user.application.result

import io.swagger.v3.oas.annotations.media.Schema

data class TokenPair(
    @field:Schema(description = "보호 API용 access JWT")
    val accessToken: String,
    @field:Schema(description = "기기별 refresh JWT")
    val refreshToken: String,
)
