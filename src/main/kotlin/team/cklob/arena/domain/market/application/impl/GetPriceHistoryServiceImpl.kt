package team.cklob.arena.domain.market.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.GetPriceHistoryService
import team.cklob.arena.domain.market.application.MarketDataCache
import team.cklob.arena.domain.market.application.MarketDataClient
import team.cklob.arena.domain.market.application.result.PriceHistoryResult
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class GetPriceHistoryServiceImpl(
    private val symbolRepository: SymbolRepository,
    private val marketDataCache: MarketDataCache,
    private val marketDataClient: MarketDataClient,
    private val marketProperties: MarketProperties,
) : GetPriceHistoryService {
    @Transactional(readOnly = true)
    override fun execute(
        symbolId: Long,
        from: LocalDate,
        to: LocalDate,
    ): PriceHistoryResult {
        val days = ChronoUnit.DAYS.between(from, to) + 1
        if (days !in 1..marketProperties.historyMaxDays) {
            throw ExpectedException(MarketErrorCode.INVALID_PRICE_HISTORY_RANGE)
        }
        val symbol = symbolRepository.findById(symbolId).orElseThrow { ExpectedException(MarketErrorCode.SYMBOL_NOT_FOUND) }
        if (symbol.market !in marketProperties.activeMarkets) throw ExpectedException(MarketErrorCode.MARKET_NOT_ACTIVE)
        if (!symbol.isActive) throw ExpectedException(MarketErrorCode.SYMBOL_NOT_ACTIVE)

        return marketDataCache.findPriceHistory(symbolId, from.toString(), to.toString())
            ?: PriceHistoryResult(
                symbolId = symbolId,
                from = from.toString(),
                to = to.toString(),
                prices = marketDataClient.fetchPriceHistory(symbol.code, from, to).sortedBy { it.snapshotAt },
            ).also(marketDataCache::savePriceHistory)
    }
}
