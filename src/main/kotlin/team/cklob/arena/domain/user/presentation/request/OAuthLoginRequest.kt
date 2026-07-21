package team.cklob.arena.domain.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.user.domain.type.ClientPlatform

data class OAuthLoginRequest(
    @field:Schema(description = "Google/Apple 및 Kakao Web OAuth authorization code", example = "authorization-code")
    val authorizationCode: String? = null,
    @field:Schema(description = "Kakao iOS/Android native SDK access token", example = "kakao-access-token")
    val accessToken: String? = null,
    @field:Schema(description = "authorization code를 발급한 클라이언트 플랫폼", example = "ANDROID")
    val platform: ClientPlatform,
    @field:Schema(description = "Apple Android PKCE state", example = "state")
    val state: String? = null,
)
