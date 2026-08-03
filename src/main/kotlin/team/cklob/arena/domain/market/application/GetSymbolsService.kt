package team.cklob.arena.domain.market.application

import team.cklob.arena.domain.market.application.result.SymbolPageResult
import team.cklob.arena.domain.market.domain.type.MarketType

interface GetSymbolsService {
    fun execute(
        market: MarketType,
        page: Int,
        size: Int,
    ): SymbolPageResult
}
