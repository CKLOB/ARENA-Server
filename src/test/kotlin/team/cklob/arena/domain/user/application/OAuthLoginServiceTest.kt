package team.cklob.arena.domain.user.application

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import team.cklob.arena.domain.user.application.impl.OnboardingServiceImpl
import team.cklob.arena.domain.user.application.impl.OAuthLoginServiceImpl
import team.cklob.arena.domain.user.application.result.ExistingUserLoginResult
import team.cklob.arena.domain.user.application.result.NewUserLoginResult
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.OAuthProviderClient
import team.cklob.arena.domain.user.infrastructure.OAuthProviderClientResolver
import team.cklob.arena.global.security.JwtProperties
import team.cklob.arena.global.security.JwtPurpose
import team.cklob.arena.global.security.JwtTokenProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import java.time.Duration
import java.util.Optional

class OAuthLoginServiceTest : DescribeSpec({
    it("신규 OAuth 사용자는 onboarding token만 받는다") {
        val client = mockk<OAuthProviderClient>()
        val users = mockk<UserRepository>()
        every { client.provider } returns OauthProvider.GOOGLE
        val resolver = OAuthProviderClientResolver(listOf(client))
        every { client.authenticate("code", null, ClientPlatform.WEB, null) } returns OAuthProfile("provider-id", null, null)
        every { users.findByOauthProviderAndOauthProviderUserId(OauthProvider.GOOGLE, "provider-id") } returns null
        val tokens = JwtTokenProvider(JwtProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15)))
        val service = OAuthLoginServiceImpl(resolver, users, mockk<RefreshSessionRepository>(), tokens, JwtProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15)), mockk<RefreshTokenStore>(relaxed = true))

        val result = service.execute(OauthProvider.GOOGLE, "code", null, ClientPlatform.WEB, null)

        result.isNewUser shouldBe true
    }

    it("기존 OAuth 사용자는 access와 refresh token을 받는다") {
        val client = mockk<OAuthProviderClient>()
        every { client.provider } returns OauthProvider.KAKAO
        val resolver = OAuthProviderClientResolver(listOf(client))
        val users = mockk<UserRepository>()
        val sessions = mockk<RefreshSessionRepository>()
        every { client.authenticate(null, "kakao-token", ClientPlatform.IOS, null) } returns OAuthProfile("provider-id", null, null)
        val user = User(nickname = "arena", investmentExperience = InvestmentExperience.BEGINNER, oauthProvider = OauthProvider.KAKAO, oauthProviderUserId = "provider-id").apply { id = 1L }
        every { users.findByOauthProviderAndOauthProviderUserId(OauthProvider.KAKAO, "provider-id") } returns user
        every { sessions.save(any()) } answers { firstArg<RefreshSession>().apply { id = 1L } }
        val properties = JwtProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15))
        val service = OAuthLoginServiceImpl(resolver, users, sessions, JwtTokenProvider(properties), properties, mockk<RefreshTokenStore>(relaxed = true))

        val result = service.execute(OauthProvider.KAKAO, null, "kakao-token", ClientPlatform.IOS, null) as ExistingUserLoginResult

        result.isNewUser shouldBe false
        result.accessToken.isBlank() shouldBe false
        result.refreshToken.isBlank() shouldBe false
    }

    it("Apple mock provider의 Android authorization code와 state를 교환한다") {
        val client = mockk<OAuthProviderClient>()
        val users = mockk<UserRepository>()
        every { client.provider } returns OauthProvider.APPLE
        every { client.authenticate("apple-code", null, ClientPlatform.ANDROID, "state") } returns OAuthProfile("apple-user-id", null, null)
        every { users.findByOauthProviderAndOauthProviderUserId(OauthProvider.APPLE, "apple-user-id") } returns null
        val properties = JwtProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15))
        val tokens = JwtTokenProvider(properties)
        val service = OAuthLoginServiceImpl(OAuthProviderClientResolver(listOf(client)), users, mockk<RefreshSessionRepository>(), tokens, properties, mockk<RefreshTokenStore>(relaxed = true))

        val result = service.execute(OauthProvider.APPLE, "apple-code", null, ClientPlatform.ANDROID, "state") as NewUserLoginResult

        tokens.getSubject(result.onboardingToken, JwtPurpose.ONBOARDING) shouldBe "APPLE:apple-user-id"
        verify(exactly = 1) { client.authenticate("apple-code", null, ClientPlatform.ANDROID, "state") }
    }

    it("온보딩을 완료하면 사용자와 session을 만들고 access와 refresh token을 발급한다") {
        val users = mockk<UserRepository>()
        val sessions = mockk<RefreshSessionRepository>()
        val properties = JwtProperties("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15))
        val tokens = JwtTokenProvider(properties)
        val savedUser = slot<User>()
        val savedSession = slot<RefreshSession>()
        every { users.save(capture(savedUser)) } answers { savedUser.captured.apply { id = 1L } }
        every { sessions.save(capture(savedSession)) } answers { savedSession.captured.apply { id = 2L } }
        val service = OnboardingServiceImpl(users, sessions, tokens, properties, mockk<RefreshTokenStore>(relaxed = true))

        val result = service.execute(tokens.createOnboardingToken("APPLE:apple-user-id"), "arena", InvestmentExperience.BEGINNER)

        savedUser.captured.oauthProvider shouldBe OauthProvider.APPLE
        savedUser.captured.oauthProviderUserId shouldBe "apple-user-id"
        tokens.getUserId(result.accessToken) shouldBe 1L
        tokens.getUserId(result.refreshToken, JwtPurpose.REFRESH) shouldBe 1L
        tokens.getRefreshSessionId(result.refreshToken) shouldBe 2L
        savedSession.captured.tokenHash shouldBe RefreshTokenHasher.hash(result.refreshToken)
    }
})
