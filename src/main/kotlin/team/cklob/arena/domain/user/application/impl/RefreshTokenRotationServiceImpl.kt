package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.RefreshTokenRotationService
import team.cklob.arena.domain.user.application.result.TokenPair
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.JwtProperties
import team.cklob.arena.global.security.JwtPurpose
import team.cklob.arena.global.security.JwtTokenProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import team.cklob.arena.global.security.SecurityErrorCode
import java.time.LocalDateTime

@Service
class RefreshTokenRotationServiceImpl(
    private val refreshSessionRepository: RefreshSessionRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenStore: RefreshTokenStore,
) : RefreshTokenRotationService {
    @Transactional
    override fun execute(refreshToken: String): TokenPair {
        val sessionId = jwtTokenProvider.getRefreshSessionId(refreshToken)
        val userId = jwtTokenProvider.getUserId(refreshToken, JwtPurpose.REFRESH)
        val session = refreshSessionRepository.findById(sessionId).orElseThrow { ExpectedException(SecurityErrorCode.INVALID_TOKEN) }
        val now = LocalDateTime.now()
        val tokenHash = RefreshTokenHasher.hash(refreshToken)
        val cachedTokenHash = refreshTokenStore.findTokenHash(sessionId)
        if (
            session.user.id != userId ||
            session.tokenHash != tokenHash ||
            (cachedTokenHash != null && cachedTokenHash != tokenHash) ||
            session.revokedAt != null ||
            !session.expiresAt.isAfter(now)
        ) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        }
        refreshTokenStore.save(sessionId, tokenHash, session.expiresAt)
        session.revoke(now)
        refreshTokenStore.delete(sessionId)
        val newSession =
            refreshSessionRepository.save(
                RefreshSession(session.user, "pending", now.plus(jwtProperties.refreshTokenExpiration)),
            )
        val newRefreshToken = jwtTokenProvider.createRefreshToken(userId, requireNotNull(newSession.id))
        newSession.tokenHash = RefreshTokenHasher.hash(newRefreshToken)
        refreshTokenStore.save(requireNotNull(newSession.id), newSession.tokenHash, newSession.expiresAt)
        return TokenPair(jwtTokenProvider.createAccessToken(userId), newRefreshToken)
    }
}
