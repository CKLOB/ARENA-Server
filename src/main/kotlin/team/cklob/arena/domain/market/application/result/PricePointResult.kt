package team.cklob.arena.domain.market.application.result

import java.math.BigDecimal
import java.time.Instant

data class PricePointResult(
    val price: BigDecimal,
    val snapshotAt: Instant,
)
