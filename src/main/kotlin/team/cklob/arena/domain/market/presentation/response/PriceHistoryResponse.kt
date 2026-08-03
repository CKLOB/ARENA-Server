package team.cklob.arena.domain.market.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.market.application.result.PriceHistoryResult

data class PriceHistoryResponse(
    @field:Schema(example = "1")
    val symbolId: Long,
    val prices: List<PricePointResponse>,
) {
    companion object {
        fun from(result: PriceHistoryResult): PriceHistoryResponse =
            PriceHistoryResponse(result.symbolId, result.prices.map(PricePointResponse::from))
    }
}
