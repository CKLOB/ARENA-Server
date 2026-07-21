package team.cklob.arena.domain.user.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.cklob.arena.domain.user.domain.entity.RefreshSession

interface RefreshSessionRepository : JpaRepository<RefreshSession, Long> {
    fun findByTokenHash(tokenHash: String): RefreshSession?
}
