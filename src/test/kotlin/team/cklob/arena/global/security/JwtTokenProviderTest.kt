package team.cklob.arena.global.security

import io.jsonwebtoken.UnsupportedJwtException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.util.Base64
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JwtTokenProviderTest : DescribeSpec({
    val tokenProvider =
        JwtTokenProvider(
            JwtProperties(
                secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                accessTokenExpiration = Duration.ofMinutes(15),
                refreshTokenExpiration = Duration.ofDays(14),
                onboardingTokenExpiration = Duration.ofMinutes(10),
            ),
        )

    describe("JWT purpose") {
        it("access token은 access 용도로만 사용자 ID를 읽는다") {
            tokenProvider.getUserId(tokenProvider.createAccessToken(1L)) shouldBe 1L
        }

        it("refresh token을 access token으로 사용하면 거부한다") {
            val refreshToken = tokenProvider.createRefreshToken(1L, 10L)

            val exception = runCatching {
                tokenProvider.getUserId(refreshToken)
            }.exceptionOrNull()

            (exception is UnsupportedJwtException) shouldBe true
        }

        it("refresh token에서 session ID를 읽는다") {
            val refreshToken = tokenProvider.createRefreshToken(1L, 10L)

            tokenProvider.getRefreshSessionId(refreshToken) shouldBe 10L
        }

        it("refresh token의 만료는 발급 시점부터 14일이다") {
            val payload =
                jacksonObjectMapper()
                    .readTree(String(Base64.getUrlDecoder().decode(tokenProvider.createRefreshToken(1L, 10L).split('.')[1])))

            (payload["exp"].asLong() - payload["iat"].asLong()) shouldBe Duration.ofDays(14).seconds
        }

        it("access token에서는 refresh session ID를 읽을 수 없다") {
            val exception = runCatching {
                tokenProvider.getRefreshSessionId(tokenProvider.createAccessToken(1L))
            }.exceptionOrNull()

            (exception is UnsupportedJwtException) shouldBe true
        }

        it("onboarding token은 onboarding 용도로만 subject를 읽는다") {
            val onboardingToken = tokenProvider.createOnboardingToken("GOOGLE:provider-user-id")

            tokenProvider.getSubject(onboardingToken, JwtPurpose.ONBOARDING) shouldBe "GOOGLE:provider-user-id"
        }

        it("서로 다른 모든 token purpose 조합을 거부한다") {
            val tokens =
                listOf(
                    tokenProvider.createAccessToken(1L) to JwtPurpose.ACCESS,
                    tokenProvider.createRefreshToken(1L, 10L) to JwtPurpose.REFRESH,
                    tokenProvider.createOnboardingToken("GOOGLE:provider-user-id") to JwtPurpose.ONBOARDING,
                )

            tokens.forEach { (token, purpose) ->
                JwtPurpose.entries.filter { it != purpose }.forEach { expectedPurpose ->
                    (runCatching { tokenProvider.getSubject(token, expectedPurpose) }.exceptionOrNull() is UnsupportedJwtException) shouldBe true
                }
            }
        }
    }
})
