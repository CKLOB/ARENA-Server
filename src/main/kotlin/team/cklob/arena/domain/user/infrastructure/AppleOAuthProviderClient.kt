package team.cklob.arena.domain.user.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.dto.AppleJwkSet
import team.cklob.arena.domain.user.infrastructure.dto.AppleTokenResponse
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.property.AppleOAuthProperties
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.time.Instant
import java.util.Base64

@Component
class AppleOAuthProviderClient(
    private val properties: OAuthProperties,
    private val stateStore: AppleOAuthStateStore,
    private val restClientBuilder: RestClient.Builder,
    private val objectMapper: ObjectMapper,
) : OAuthProviderClient {
    override val provider = OauthProvider.APPLE

    override fun authenticate(
        authorizationCode: String?,
        accessToken: String?,
        platform: ClientPlatform,
        state: String?,
    ): OAuthProfile =
        try {
            val apple = properties.apple
            val code = authorizationCode?.takeIf(String::isNotBlank) ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
            val clientId = apple.clientId(platform)
            val redirectUri = apple.androidRedirectUri.takeIf(String::isNotBlank)
            val verifier =
                if (platform == ClientPlatform.ANDROID) {
                    state?.let(stateStore::consume)
                        ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
                } else {
                    null
                }
            if (platform == ClientPlatform.ANDROID && redirectUri == null) {
                throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
            }
            val token =
                restClientBuilder
                    .build()
                    .post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(
                        LinkedMultiValueMap<String, String>().apply {
                            add("client_id", clientId)
                            add("client_secret", createClientSecret(apple, clientId))
                            add("code", code)
                            add("grant_type", "authorization_code")
                            redirectUri?.let { add("redirect_uri", it) }
                            verifier?.let { add("code_verifier", it) }
                        },
                    ).retrieve()
                    .body(AppleTokenResponse::class.java)
                    ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
            val claims = verifyIdToken(token.idToken, clientId)

            OAuthProfile(claims.subject, claims["email"] as String?, null)
        } catch (exception: RestClientException) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        } catch (exception: JwtException) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        } catch (exception: IllegalArgumentException) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        } catch (exception: java.security.GeneralSecurityException) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        }

    private fun AppleOAuthProperties.clientId(platform: ClientPlatform): String =
        when (platform) {
            ClientPlatform.IOS -> iosClientId
            ClientPlatform.ANDROID -> androidClientId
            ClientPlatform.WEB -> throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        }.takeIf(String::isNotBlank) ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)

    private fun createClientSecret(
        apple: AppleOAuthProperties,
        clientId: String,
    ): String {
        val now = Instant.now()
        return Jwts
            .builder()
            .issuer(apple.teamId)
            .subject(clientId)
            .audience()
            .add(APPLE_ISSUER)
            .and()
            .issuedAt(java.util.Date.from(now))
            .expiration(java.util.Date.from(now.plus(CLIENT_SECRET_TTL)))
            .header()
            .keyId(apple.keyId)
            .and()
            .signWith(applePrivateKey(apple.privateKey), Jwts.SIG.ES256)
            .compact()
    }

    private fun verifyIdToken(
        idToken: String,
        clientId: String,
    ) = Jwts
        .parser()
        .verifyWith(applePublicKey(idToken))
        .requireIssuer(APPLE_ISSUER)
        .requireAudience(clientId)
        .build()
        .parseSignedClaims(idToken)
        .payload

    private fun applePublicKey(idToken: String): RSAPublicKey {
        val kid = objectMapper.readTree(Base64.getUrlDecoder().decode(idToken.substringBefore('.'))).required("kid").asText()
        val keys =
            restClientBuilder
                .build()
                .get()
                .uri(KEYS_URI)
                .retrieve()
                .body(AppleJwkSet::class.java)
                ?.keys
                ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        val key = keys.firstOrNull { it.kid == kid && it.kty == "RSA" } ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        return KeyFactory
            .getInstance("RSA")
            .generatePublic(
                RSAPublicKeySpec(
                    BigInteger(1, Base64.getUrlDecoder().decode(key.modulus)),
                    BigInteger(1, Base64.getUrlDecoder().decode(key.exponent)),
                ),
            ) as RSAPublicKey
    }

    private fun applePrivateKey(pem: String): ECPrivateKey =
        KeyFactory
            .getInstance("EC")
            .generatePrivate(
                PKCS8EncodedKeySpec(
                    Base64.getMimeDecoder().decode(pem.replace("\\n", "\n").replace(PEM_HEADER_FOOTER, "")),
                ),
            ) as ECPrivateKey

    private companion object {
        const val APPLE_ISSUER = "https://appleid.apple.com"
        const val TOKEN_URI = "$APPLE_ISSUER/auth/token"
        const val KEYS_URI = "$APPLE_ISSUER/auth/keys"
        val CLIENT_SECRET_TTL = Duration.ofDays(180)
        val PEM_HEADER_FOOTER = Regex("-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\\s")
    }
}
