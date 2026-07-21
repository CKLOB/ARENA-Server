package team.cklob.arena.domain.user.infrastructure

import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile

interface OAuthProviderClient {
    val provider: OauthProvider

    fun authenticate(
        authorizationCode: String?,
        accessToken: String?,
        platform: ClientPlatform,
        state: String? = null,
    ): OAuthProfile
}
