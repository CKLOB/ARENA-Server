package team.cklob.arena.domain.recommendation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import team.cklob.arena.domain.challenge.Challenge
import team.cklob.arena.domain.market.domain.entity.Symbol
import java.math.BigDecimal
import java.time.LocalDateTime

enum class RecommendationReaction {
    ACCEPTED,
    REJECTED,
    IGNORED,
}

@Entity
@Table(
    name = "recommendations",
    indexes = [
        Index(name = "idx_recommendations_challenge_id", columnList = "challenge_id"),
        Index(name = "idx_recommendations_symbol_id", columnList = "symbol_id"),
    ],
)
class Recommendation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    var challenge: Challenge,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    var symbol: Symbol,
    @Column(name = "reason_text", nullable = false, length = 500)
    var reasonText: String,
    @Column(name = "probability", nullable = false, precision = 5, scale = 4)
    var probability: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction", length = 20)
    var reaction: RecommendationReaction? = null,
    @Column(name = "reacted_at")
    var reactedAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
}

interface RecommendationRepository : JpaRepository<Recommendation, Long>
