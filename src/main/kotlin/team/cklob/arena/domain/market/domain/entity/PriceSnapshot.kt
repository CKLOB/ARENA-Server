package team.cklob.arena.domain.market.domain.entity

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
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "price_snapshots",
    indexes = [
        Index(name = "idx_price_snapshots_symbol_snapshot", columnList = "symbol_id, snapshot_at"),
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
