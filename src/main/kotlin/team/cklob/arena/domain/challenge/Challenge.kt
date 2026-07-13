package team.cklob.arena.domain.challenge

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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository
import team.cklob.arena.domain.market.MarketType
import team.cklob.arena.domain.user.User
import java.math.BigDecimal
import java.time.LocalDateTime

enum class AiStrategy {
    STABLE,
    AGGRESSIVE,
    TREND,
}

enum class ChallengeStatus {
    ACTIVE,
    ENDED,
}

enum class ParticipantType {
    USER,
    AI,
}

@Entity
@Table(
    name = "challenges",
    indexes = [
        Index(name = "idx_challenges_user_id", columnList = "user_id"),
        Index(name = "idx_challenges_status", columnList = "status"),
    ],
)
class Challenge(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 10)
    var market: MarketType,
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_strategy", nullable = false, length = 20)
    var aiStrategy: AiStrategy,
    @Column(name = "seed_amount", nullable = false, precision = 18, scale = 2)
    var seedAmount: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ChallengeStatus = ChallengeStatus.ACTIVE,
    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime,
    @Column(name = "ended_at")
    var endedAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface ChallengeRepository : JpaRepository<Challenge, Long>

@Entity
@Table(
    name = "challenge_participants",
    indexes = [
        Index(name = "idx_participants_challenge_id", columnList = "challenge_id"),
    ],
)
class ChallengeParticipant(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    var challenge: Challenge,
    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false, length = 10)
    var participantType: ParticipantType,
    @Column(name = "current_balance", nullable = false, precision = 18, scale = 2)
    var currentBalance: BigDecimal,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface ChallengeParticipantRepository : JpaRepository<ChallengeParticipant, Long>

@Entity
@Table(
    name = "challenge_results",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_challenge_results_challenge_id", columnNames = ["challenge_id"]),
    ],
)
class ChallengeResult(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    var challenge: Challenge,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_participant_id", nullable = false)
    var winnerParticipant: ChallengeParticipant,
    @Column(name = "final_return_rate", nullable = false, precision = 7, scale = 4)
    var finalReturnRate: BigDecimal,
    @Column(name = "gpt_feedback_text", columnDefinition = "TEXT")
    var gptFeedbackText: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface ChallengeResultRepository : JpaRepository<ChallengeResult, Long>
