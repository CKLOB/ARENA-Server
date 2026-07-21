package team.cklob.arena.domain.user.infrastructure

import org.springframework.http.MediaType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.dto.GoogleTokenResponse
import team.cklob.arena.domain.user.infrastructure.dto.GoogleUserInfoResponse
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@Component
@ConditionalOnProperty(name = ["oauth.google.enabled"], havingValue = "true", matchIfMissing = true)
class GoogleOAuthProviderClient(
    private val properties: OAuthProperties,
    private val restClientBuilder: RestClient.Builder,
) : OAuthProviderClient {
    override val provider = OauthProvider.GOOGLE

    override fun authenticate(
        authorizationCode: String?,
        accessToken: String?,
        platform: ClientPlatform,
        state: String?,
    ): OAuthProfile =
        try {
            val code = authorizationCode?.takeIf(String::isNotBlank) ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
            val google = properties.google
            val redirectUri = requireRedirectUri(google.redirectUris, platform)
            val token =
                restClientBuilder
                    .build()
                    .post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(
                        LinkedMultiValueMap<String, String>().apply {
                            add("code", code)
                            add("client_id", google.clientId)
                            add("client_secret", google.clientSecret)
                            add("redirect_uri", redirectUri)
                            add("grant_type", "authorization_code")
                        },
                    ).retrieve()
                    .body(GoogleTokenResponse::class.java)
                    ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
            val profile =
                restClientBuilder
                    .build()
                    .get()
                    .uri(USER_INFO_URI)
                    .headers { it.setBearerAuth(token.accessToken) }
                    .retrieve()
                    .body(GoogleUserInfoResponse::class.java)
                    ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)

            OAuthProfile(profile.subject, profile.email, profile.picture)
        } catch (exception: RestClientException) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        }

    private fun requireRedirectUri(
        redirectUris: Map<ClientPlatform, String>,
        platform: ClientPlatform,
    ): String = redirectUris[platform]?.takeIf(String::isNotBlank) ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)

    private companion object {
        const val TOKEN_URI = "https://oauth2.googleapis.com/token"
        const val USER_INFO_URI = "https://openidconnect.googleapis.com/v1/userinfo"
    }
}
