package team.cklob.arena.domain.market.application

import team.cklob.arena.domain.market.application.result.CurrentPriceResult
import team.cklob.arena.domain.market.application.result.PriceHistoryResult
import team.cklob.arena.domain.market.domain.type.MarketType

interface MarketDataCache {
    fun findCurrentPrice(symbolId: Long): CurrentPriceResult?

    fun saveCurrentPrice(
        market: MarketType,
        result: CurrentPriceResult,
    )

    fun findPriceHistory(
        symbolId: Long,
        from: String,
        to: String,
    ): PriceHistoryResult?

    fun savePriceHistory(result: PriceHistoryResult)
}
