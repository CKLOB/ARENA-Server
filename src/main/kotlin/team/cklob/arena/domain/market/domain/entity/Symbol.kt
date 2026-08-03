package team.cklob.arena.domain.market.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.cklob.arena.domain.market.domain.type.MarketType

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
    val market: MarketType,
    @Column(name = "code", nullable = false, length = 30)
    val code: String,
    @Column(name = "name", nullable = false, length = 150)
    var name: String,
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun synchronize(name: String) {
        this.name = name
        isActive = true
    }

    fun deactivate() {
        isActive = false
    }
}
