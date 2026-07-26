package team.cklob.arena.domain.user.infrastructure

import java.time.LocalDateTime

interface RefreshTokenStore {
    fun save(
        sessionId: Long,
        tokenHash: String,
        expiresAt: LocalDateTime,
    )

    fun findTokenHash(sessionId: Long): String?

    fun delete(sessionId: Long)
}
