package team.cklob.arena.domain.user.infrastructure

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import team.cklob.arena.domain.user.infrastructure.dto.AppleJwkSet
import team.cklob.arena.domain.user.infrastructure.dto.AppleTokenResponse
import team.cklob.arena.domain.user.infrastructure.dto.GoogleTokenResponse
import team.cklob.arena.domain.user.infrastructure.dto.GoogleUserInfoResponse
import team.cklob.arena.domain.user.infrastructure.dto.KakaoTokenResponse
import team.cklob.arena.domain.user.infrastructure.dto.KakaoUserInfoResponse

class OAuthProviderResponseDtoTest : DescribeSpec({
    val objectMapper = jacksonObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    it("provider 응답의 추가 필드를 무시한다") {
        objectMapper.readValue<GoogleTokenResponse>(
            """{"access_token":"google-token","token_type":"Bearer","expires_in":3600}""",
        ).accessToken shouldBe "google-token"
        objectMapper.readValue<GoogleUserInfoResponse>(
            """{"sub":"google-id","email":"user@example.com","picture":null,"locale":"ko"}""",
        ).subject shouldBe "google-id"
        objectMapper.readValue<KakaoTokenResponse>(
            """{"access_token":"kakao-token","token_type":"bearer","expires_in":21599}""",
        ).accessToken shouldBe "kakao-token"
        objectMapper.readValue<KakaoUserInfoResponse>(
            """
            {
              "id": 123,
              "connected_at": "2026-01-01T00:00:00Z",
              "kakao_account": {
                "email": "user@example.com",
                "is_email_valid": true,
                "profile": {"profile_image_url": null, "thumbnail_image_url": null}
              }
            }
            """.trimIndent(),
        ).id shouldBe 123L
        objectMapper.readValue<AppleTokenResponse>(
            """{"id_token":"apple-token","token_type":"Bearer","expires_in":3600}""",
        ).idToken shouldBe "apple-token"
        objectMapper.readValue<AppleJwkSet>(
            """{"keys":[{"kid":"key-id","kty":"RSA","alg":"RS256","n":"modulus","e":"exponent"}]}""",
        ).keys.single().kid shouldBe "key-id"
    }
})
