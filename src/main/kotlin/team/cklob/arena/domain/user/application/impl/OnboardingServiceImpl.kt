package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.OnboardingService
import team.cklob.arena.domain.user.application.result.TokenPair
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.global.security.JwtProperties
import team.cklob.arena.global.security.JwtPurpose
import team.cklob.arena.global.security.JwtTokenProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import java.time.LocalDateTime

@Service
class OnboardingServiceImpl(
    private val userRepository: UserRepository,
    private val refreshSessionRepository: RefreshSessionRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenStore: RefreshTokenStore,
) : OnboardingService {
    @Transactional
    override fun execute(
        onboardingToken: String,
        nickname: String,
        investmentExperience: InvestmentExperience,
    ): TokenPair {
        val subject = jwtTokenProvider.getSubject(onboardingToken, JwtPurpose.ONBOARDING)
        val (providerName, providerUserId) = subject.split(":", limit = 2)
        val user =
            userRepository.save(
                User(
                    nickname = nickname,
                    investmentExperience = investmentExperience,
                    oauthProvider = OauthProvider.valueOf(providerName),
                    oauthProviderUserId = providerUserId,
                ),
            )
        val session =
            refreshSessionRepository.save(
                RefreshSession(user, "pending", LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration)),
            )
        val refreshToken = jwtTokenProvider.createRefreshToken(requireNotNull(user.id), requireNotNull(session.id))
        session.tokenHash = RefreshTokenHasher.hash(refreshToken)
        refreshTokenStore.save(requireNotNull(session.id), session.tokenHash, session.expiresAt)
        return TokenPair(jwtTokenProvider.createAccessToken(requireNotNull(user.id)), refreshToken)
    }
}
