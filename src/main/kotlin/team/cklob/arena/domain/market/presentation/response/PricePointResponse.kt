package team.cklob.arena.domain.market.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.market.application.result.PricePointResult
import java.math.BigDecimal
import java.time.Instant

data class PricePointResponse(
    @field:Schema(example = "224.50")
    val price: BigDecimal,
    @field:Schema(example = "2026-08-02T09:00:00Z")
    val snapshotAt: Instant,
) {
    companion object {
        fun from(result: PricePointResult): PricePointResponse = PricePointResponse(result.price, result.snapshotAt)
    }
}
