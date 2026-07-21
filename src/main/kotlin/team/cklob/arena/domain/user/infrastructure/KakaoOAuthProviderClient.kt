package team.cklob.arena.domain.user.infrastructure

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.dto.KakaoTokenResponse
import team.cklob.arena.domain.user.infrastructure.dto.KakaoUserInfoResponse
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@Component
class KakaoOAuthProviderClient(
    private val properties: OAuthProperties,
    private val restClientBuilder: RestClient.Builder,
) : OAuthProviderClient {
    override val provider = OauthProvider.KAKAO

    override fun authenticate(
        authorizationCode: String?,
        accessToken: String?,
        platform: ClientPlatform,
        state: String?,
    ): OAuthProfile =
        try {
            val token = accessToken?.takeIf(String::isNotBlank) ?: exchangeAuthorizationCode(authorizationCode, platform)
            getProfile(token)
        } catch (exception: RestClientException) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        }

    private fun exchangeAuthorizationCode(authorizationCode: String?, platform: ClientPlatform): String {
        val code = authorizationCode?.takeIf(String::isNotBlank) ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        val kakao = properties.kakao
        val redirectUri = kakao.redirectUris[platform]?.takeIf(String::isNotBlank) ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        return restClientBuilder
            .build()
            .post()
            .uri(TOKEN_URI)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                LinkedMultiValueMap<String, String>().apply {
                    add("grant_type", "authorization_code")
                    add("client_id", kakao.clientId)
                    add("client_secret", kakao.clientSecret)
                    add("redirect_uri", redirectUri)
                    add("code", code)
                },
            ).retrieve()
            .body(KakaoTokenResponse::class.java)
            ?.accessToken
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
    }

    private fun getProfile(accessToken: String): OAuthProfile {
        val profile = restClientBuilder
            .build()
            .get()
            .uri(USER_INFO_URI)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .body(KakaoUserInfoResponse::class.java)
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        return OAuthProfile(profile.id.toString(), profile.account?.email, profile.account?.profile?.imageUrl)
    }

    private companion object {
        const val TOKEN_URI = "https://kauth.kakao.com/oauth/token"
        const val USER_INFO_URI = "https://kapi.kakao.com/v2/user/me"
    }
}
