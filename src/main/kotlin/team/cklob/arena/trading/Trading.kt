package team.cklob.arena.trading

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
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import team.cklob.arena.challenge.ChallengeParticipant
import java.math.BigDecimal
import java.time.LocalDateTime

enum class OrderSide {
    BUY,
    SELL,
}

@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_participant_id", columnList = "participant_id"),
    ],
)
class Order(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    var participant: ChallengeParticipant,
    @Column(name = "symbol", nullable = false, length = 20)
    var symbol: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    var side: OrderSide,
    @Column(name = "quantity", nullable = false, precision = 18, scale = 8)
    var quantity: BigDecimal,
    @Column(name = "reason_tag", length = 50)
    var reasonTag: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
}

interface OrderRepository : JpaRepository<Order, Long>

@Entity
@Table(
    name = "executions",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_executions_order_id", columnNames = ["order_id"]),
    ],
)
class Execution(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,
    @Column(name = "executed_price", nullable = false, precision = 18, scale = 4)
    var executedPrice: BigDecimal,
    @Column(name = "executed_at", nullable = false)
    var executedAt: LocalDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface ExecutionRepository : JpaRepository<Execution, Long>

@Entity
@Table(
    name = "holdings",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_holdings_participant_symbol", columnNames = ["participant_id", "symbol"]),
    ],
    indexes = [
        Index(name = "idx_holdings_participant_id", columnList = "participant_id"),
    ],
)
class Holding(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    var participant: ChallengeParticipant,
    @Column(name = "symbol", nullable = false, length = 20)
    var symbol: String,
    @Column(name = "quantity", nullable = false, precision = 18, scale = 8)
    var quantity: BigDecimal,
    @Column(name = "avg_buy_price", nullable = false, precision = 18, scale = 4)
    var avgBuyPrice: BigDecimal,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface HoldingRepository : JpaRepository<Holding, Long>
