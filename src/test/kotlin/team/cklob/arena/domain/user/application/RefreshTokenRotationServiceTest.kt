package team.cklob.arena.domain.user.application

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import team.cklob.arena.domain.user.application.impl.RefreshTokenRotationServiceImpl
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.global.security.JwtProperties
import team.cklob.arena.global.security.JwtPurpose
import team.cklob.arena.global.security.JwtTokenProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import team.cklob.arena.global.exception.ExpectedException
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional

class RefreshTokenRotationServiceTest : DescribeSpec({
    val repository = mockk<RefreshSessionRepository>()
    val tokenProvider =
        JwtTokenProvider(
            JwtProperties(
                secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                accessTokenExpiration = Duration.ofMinutes(15),
            ),
        )
    val refreshTokenStore = mockk<RefreshTokenStore>(relaxed = true)
    val service = RefreshTokenRotationServiceImpl(repository, tokenProvider, JwtProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15)), refreshTokenStore)

    it("유효한 refresh token은 이전 session을 폐기하고 새 token pair를 발급한다") {
        val user = User(nickname = "arena", investmentExperience = InvestmentExperience.BEGINNER, oauthProvider = OauthProvider.GOOGLE, oauthProviderUserId = "provider-id").apply { id = 1L }
        val currentToken = tokenProvider.createRefreshToken(1L, 10L)
        val session = RefreshSession(user, RefreshTokenHasher.hash(currentToken), LocalDateTime.now().plusDays(1)).apply { id = 10L }
        val newSession = slot<RefreshSession>()

        every { repository.findById(10L) } returns Optional.of(session)
        every { repository.save(capture(newSession)) } answers { newSession.captured.apply { id = 11L } }
        every { refreshTokenStore.findTokenHash(10L) } returns RefreshTokenHasher.hash(currentToken)

        val tokens = service.execute(currentToken)

        (session.revokedAt != null) shouldBe true
        tokenProvider.getUserId(tokens.accessToken) shouldBe 1L
        tokenProvider.getRefreshSessionId(tokens.refreshToken) shouldBe 11L
        newSession.captured.tokenHash shouldBe RefreshTokenHasher.hash(tokens.refreshToken)
    }

    it("폐기된 session의 refresh token 재사용을 거부한다") {
        val user = User(nickname = "arena", investmentExperience = InvestmentExperience.BEGINNER, oauthProvider = OauthProvider.GOOGLE, oauthProviderUserId = "provider-id-2").apply { id = 2L }
        val token = tokenProvider.createRefreshToken(2L, 20L)
        val session = RefreshSession(user, RefreshTokenHasher.hash(token), LocalDateTime.now().plusDays(1), LocalDateTime.now()).apply { id = 20L }

        every { repository.findById(20L) } returns Optional.of(session)

        val exception = runCatching { service.execute(token) }.exceptionOrNull()

        (exception is ExpectedException) shouldBe true
    }
})
