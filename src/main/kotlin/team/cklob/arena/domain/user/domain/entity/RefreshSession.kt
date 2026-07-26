package team.cklob.arena.domain.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_sessions",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_refresh_sessions_token_hash", columnNames = ["token_hash"]),
    ],
    indexes = [
        Index(name = "idx_refresh_sessions_user_id", columnList = "user_id"),
    ],
)
class RefreshSession(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    @Column(name = "token_hash", nullable = false, length = 64)
    var tokenHash: String,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,
    @Column(name = "revoked_at")
    var revokedAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    fun revoke(now: LocalDateTime) {
        revokedAt = now
    }
}
