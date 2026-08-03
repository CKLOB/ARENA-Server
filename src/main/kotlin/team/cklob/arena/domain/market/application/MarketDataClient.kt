package team.cklob.arena.domain.market.application

import team.cklob.arena.domain.market.application.result.ExternalSymbolResult
import team.cklob.arena.domain.market.application.result.PricePointResult
import team.cklob.arena.domain.market.application.result.PriceQuoteResult
import java.time.LocalDate

interface MarketDataClient {
    fun fetchSymbols(): List<ExternalSymbolResult>

    fun fetchCurrentPrice(code: String): PriceQuoteResult

    fun fetchPriceHistory(
        code: String,
        from: LocalDate,
        to: LocalDate,
    ): List<PricePointResult>
}
