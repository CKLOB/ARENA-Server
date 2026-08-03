package team.cklob.arena.domain.market.application.result

import team.cklob.arena.domain.market.domain.entity.Symbol

data class SymbolSummaryResult(
    val symbolId: Long,
    val code: String,
    val name: String,
    val isActive: Boolean,
) {
    companion object {
        fun from(symbol: Symbol): SymbolSummaryResult =
            SymbolSummaryResult(
                symbolId = requireNotNull(symbol.id),
                code = symbol.code,
                name = symbol.name,
                isActive = symbol.isActive,
            )
    }
}
