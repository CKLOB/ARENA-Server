package team.cklob.arena.domain.market.application.impl

import org.springframework.stereotype.Service
import team.cklob.arena.domain.market.application.GetCurrentPriceService
import team.cklob.arena.domain.market.application.MarketDataCache
import team.cklob.arena.domain.market.application.MarketDataClient
import team.cklob.arena.domain.market.application.MarketValidator
import team.cklob.arena.domain.market.application.result.CurrentPriceResult

@Service
class GetCurrentPriceServiceImpl(
    private val marketValidator: MarketValidator,
    private val marketDataCache: MarketDataCache,
    private val marketDataClient: MarketDataClient,
) : GetCurrentPriceService {
    override fun execute(symbolId: Long): CurrentPriceResult {
        val symbol = marketValidator.findActiveSymbol(symbolId)

        return marketDataCache.findCurrentPrice(symbolId) ?: marketDataClient.fetchCurrentPrice(symbol.code).let {
            CurrentPriceResult(symbolId, it.price, it.snapshotAt).also { result ->
                marketDataCache.saveCurrentPrice(symbol.market, result)
            }
        }
    }
}
