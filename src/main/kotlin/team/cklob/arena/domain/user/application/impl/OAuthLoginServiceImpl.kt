package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.OAuthLoginResult
import team.cklob.arena.domain.user.application.OAuthLoginService
import team.cklob.arena.domain.user.application.result.ExistingUserLoginResult
import team.cklob.arena.domain.user.application.result.NewUserLoginResult
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.OAuthProviderClientResolver
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.global.security.JwtProperties
import team.cklob.arena.global.security.JwtTokenProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import java.time.LocalDateTime

@Service
class OAuthLoginServiceImpl(
    private val clientResolver: OAuthProviderClientResolver,
    private val userRepository: UserRepository,
    private val refreshSessionRepository: RefreshSessionRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenStore: RefreshTokenStore,
) : OAuthLoginService {
    @Transactional
    override fun execute(
        provider: OauthProvider,
        authorizationCode: String?,
        accessToken: String?,
        platform: ClientPlatform,
        state: String?,
    ): OAuthLoginResult {
        val profile = clientResolver.resolve(provider).authenticate(authorizationCode, accessToken, platform, state)
        val user =
            userRepository.findByOauthProviderAndOauthProviderUserIdForUpdate(provider, profile.providerUserId)
                ?: return NewUserLoginResult(jwtTokenProvider.createOnboardingToken("${provider.name}:${profile.providerUserId}"))
        user.restore()
        val session =
            refreshSessionRepository.save(
                RefreshSession(user, "pending", LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration)),
            )
        val refreshToken =
            jwtTokenProvider.createRefreshToken(
                requireNotNull(user.id),
                requireNotNull(session.id),
                user.authVersion,
            )
        session.tokenHash = RefreshTokenHasher.hash(refreshToken)
        refreshTokenStore.save(requireNotNull(session.id), session.tokenHash, session.expiresAt)
        return ExistingUserLoginResult(jwtTokenProvider.createAccessToken(requireNotNull(user.id), user.authVersion), refreshToken)
    }
}
