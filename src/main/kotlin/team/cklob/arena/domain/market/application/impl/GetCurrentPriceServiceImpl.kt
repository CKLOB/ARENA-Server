package team.cklob.arena.domain.market.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.GetCurrentPriceService
import team.cklob.arena.domain.market.application.MarketDataCache
import team.cklob.arena.domain.market.application.MarketDataClient
import team.cklob.arena.domain.market.application.result.CurrentPriceResult
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException

@Service
class GetCurrentPriceServiceImpl(
    private val symbolRepository: SymbolRepository,
    private val marketDataCache: MarketDataCache,
    private val marketDataClient: MarketDataClient,
    private val marketProperties: MarketProperties,
) : GetCurrentPriceService {
    @Transactional(readOnly = true)
    override fun execute(symbolId: Long): CurrentPriceResult {
        val symbol = symbolRepository.findById(symbolId).orElseThrow { ExpectedException(MarketErrorCode.SYMBOL_NOT_FOUND) }
        if (symbol.market !in marketProperties.activeMarkets) throw ExpectedException(MarketErrorCode.MARKET_NOT_ACTIVE)
        if (!symbol.isActive) throw ExpectedException(MarketErrorCode.SYMBOL_NOT_ACTIVE)

        return marketDataCache.findCurrentPrice(symbolId) ?: marketDataClient.fetchCurrentPrice(symbol.code).let {
            CurrentPriceResult(symbolId, it.price, it.snapshotAt).also { result ->
                marketDataCache.saveCurrentPrice(symbol.market, result)
            }
        }
    }
}
