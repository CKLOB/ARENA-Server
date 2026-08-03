package team.cklob.arena.domain.market.application.result

import team.cklob.arena.domain.market.domain.type.MarketType

data class ExternalSymbolResult(
    val market: MarketType,
    val code: String,
    val name: String,
)
