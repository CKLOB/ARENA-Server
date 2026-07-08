package team.cklob.arena.market

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
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal
import java.time.LocalDateTime

enum class MarketType {
    KR,
    US,
    COIN,
}

@Entity
@Table(
    name = "symbols",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_symbols_market_code", columnNames = ["market", "code"]),
    ],
)
class Symbol(
    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 10)
    var market: MarketType,
    @Column(name = "code", nullable = false, length = 20)
    var code: String,
    @Column(name = "name", nullable = false, length = 100)
    var name: String,
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface SymbolRepository : JpaRepository<Symbol, Long>

@Entity
@Table(
    name = "price_snapshots",
    indexes = [
        Index(name = "idx_price_snapshots_symbol_id", columnList = "symbol_id"),
        Index(name = "idx_price_snapshots_snapshot_at", columnList = "snapshot_at"),
    ],
)
class PriceSnapshot(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    var symbol: Symbol,
    @Column(name = "price", nullable = false, precision = 18, scale = 4)
    var price: BigDecimal,
    @Column(name = "snapshot_at", nullable = false)
    var snapshotAt: LocalDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface PriceSnapshotRepository : JpaRepository<PriceSnapshot, Long>
