package team.cklob.arena.domain.user.infrastructure

import org.springframework.stereotype.Component
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@Component
class OAuthProviderClientResolver(
    clients: List<OAuthProviderClient>,
) {
    private val clientsByProvider = clients.associateBy { it.provider }

    fun resolve(provider: OauthProvider): OAuthProviderClient =
        clientsByProvider[provider] ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
}
