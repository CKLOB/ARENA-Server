package team.cklob.arena.domain.user.application

import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider

interface OAuthLoginService {
    fun execute(
        provider: OauthProvider,
        authorizationCode: String?,
        accessToken: String?,
        platform: ClientPlatform,
        state: String?,
    ): OAuthLoginResult
}
