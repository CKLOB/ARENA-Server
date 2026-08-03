package team.cklob.arena.domain.market.application.result

data class PriceHistoryResult(
    val symbolId: Long,
    val from: String,
    val to: String,
    val prices: List<PricePointResult>,
)
