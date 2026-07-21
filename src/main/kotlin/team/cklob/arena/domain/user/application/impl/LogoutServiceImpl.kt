package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.LogoutService
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.JwtPurpose
import team.cklob.arena.global.security.JwtTokenProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import team.cklob.arena.global.security.SecurityErrorCode
import java.time.LocalDateTime

@Service
class LogoutServiceImpl(
    private val refreshSessionRepository: RefreshSessionRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenStore: RefreshTokenStore,
) : LogoutService {
    @Transactional
    override fun execute(refreshToken: String) {
        val sessionId = jwtTokenProvider.getRefreshSessionId(refreshToken)
        val userId = jwtTokenProvider.getUserId(refreshToken, JwtPurpose.REFRESH)
        val session = refreshSessionRepository.findById(sessionId).orElseThrow { ExpectedException(SecurityErrorCode.INVALID_TOKEN) }
        val tokenHash = RefreshTokenHasher.hash(refreshToken)
        val cachedTokenHash = refreshTokenStore.findTokenHash(sessionId)
        if (session.user.id != userId || session.tokenHash != tokenHash || (cachedTokenHash != null && cachedTokenHash != tokenHash) || session.revokedAt != null) {
            throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
        }
        refreshTokenStore.save(sessionId, tokenHash, session.expiresAt)
        session.revoke(LocalDateTime.now())
        refreshTokenStore.delete(sessionId)
    }
}
