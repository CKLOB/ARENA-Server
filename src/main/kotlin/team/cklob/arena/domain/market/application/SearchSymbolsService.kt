package team.cklob.arena.domain.market.application

import team.cklob.arena.domain.market.application.result.SymbolPageResult
import team.cklob.arena.domain.market.domain.type.MarketType

interface SearchSymbolsService {
    fun execute(
        market: MarketType,
        keyword: String,
        page: Int,
        size: Int,
    ): SymbolPageResult
}
