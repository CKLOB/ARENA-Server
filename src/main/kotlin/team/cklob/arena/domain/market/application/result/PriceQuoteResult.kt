package team.cklob.arena.domain.market.application.result

import java.math.BigDecimal
import java.time.Instant

data class PriceQuoteResult(
    val price: BigDecimal,
    val snapshotAt: Instant,
)
