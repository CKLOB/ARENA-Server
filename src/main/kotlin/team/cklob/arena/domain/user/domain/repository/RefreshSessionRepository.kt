package team.cklob.arena.domain.user.domain.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.cklob.arena.domain.user.domain.entity.RefreshSession

interface RefreshSessionRepository : JpaRepository<RefreshSession, Long> {
    fun findByTokenHash(tokenHash: String): RefreshSession?

    fun findAllByUserIdAndRevokedAtIsNull(userId: Long): List<RefreshSession>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from RefreshSession session where session.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): RefreshSession?
}
