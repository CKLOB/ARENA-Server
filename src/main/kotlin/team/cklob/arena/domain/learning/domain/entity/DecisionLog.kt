package team.cklob.arena.domain.learning.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import team.cklob.arena.domain.learning.domain.type.TradingAction
import team.cklob.arena.domain.market.domain.type.MarketType
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "decision_logs",
    indexes = [
        Index(name = "idx_decision_logs_challenge_id", columnList = "challenge_id"),
        Index(name = "idx_decision_logs_market", columnList = "market"),
        Index(name = "idx_decision_logs_model_version", columnList = "model_version"),
        Index(name = "idx_decision_logs_decided_at", columnList = "decided_at"),
    ],
)
class DecisionLog(
    @Column(name = "challenge_id", nullable = false)
    var challengeId: Long,
    @Column(name = "symbol_code", nullable = false, length = 20)
    var symbolCode: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 10)
    var market: MarketType,
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 10)
    var action: TradingAction,
    @Column(name = "probability", nullable = false, precision = 5, scale = 4)
    var probability: BigDecimal,
    @Column(name = "model_version", nullable = false, length = 100)
    var modelVersion: String,
    @Column(name = "decided_at", nullable = false)
    var decidedAt: LocalDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
}
