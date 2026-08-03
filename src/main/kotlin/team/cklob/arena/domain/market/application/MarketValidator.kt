package team.cklob.arena.domain.market.application

import org.springframework.stereotype.Component
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException

@Component
class MarketValidator(
    private val symbolRepository: SymbolRepository,
    private val marketProperties: MarketProperties,
) {
    fun requireActiveMarket(market: MarketType) {
        if (market !in marketProperties.activeMarkets) throw ExpectedException(MarketErrorCode.MARKET_NOT_ACTIVE)
    }

    fun findActiveSymbol(symbolId: Long): Symbol {
        val symbol = symbolRepository.findById(symbolId).orElseThrow { ExpectedException(MarketErrorCode.SYMBOL_NOT_FOUND) }
        requireActiveMarket(symbol.market)
        if (!symbol.isActive) throw ExpectedException(MarketErrorCode.SYMBOL_NOT_ACTIVE)
        return symbol
    }
}
