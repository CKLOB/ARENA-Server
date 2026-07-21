package team.cklob.arena.domain.user.infrastructure

import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@Service
class AppleOAuthAuthorizationServiceImpl(
    private val properties: OAuthProperties,
    private val stateStore: AppleOAuthStateStore,
) : AppleOAuthAuthorizationService {
    override fun execute(): String {
        val apple = properties.apple
        val clientId = apple.androidClientId.takeIf(String::isNotBlank)
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        val redirectUri = apple.androidRedirectUri.takeIf(String::isNotBlank)
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        val state = stateStore.create()
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URI)
            .queryParam("response_type", "code")
            .queryParam("response_mode", "query")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", "name email")
            .queryParam("state", state.state)
            .queryParam("code_challenge", state.challenge)
            .queryParam("code_challenge_method", "S256")
            .build()
            .encode()
            .toUriString()
    }

    private companion object {
        const val AUTHORIZE_URI = "https://appleid.apple.com/auth/authorize"
    }
}
