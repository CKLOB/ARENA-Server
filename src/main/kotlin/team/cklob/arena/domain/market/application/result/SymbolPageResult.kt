package team.cklob.arena.domain.market.application.result

import org.springframework.data.domain.Page
import team.cklob.arena.domain.market.domain.entity.Symbol

data class SymbolPageResult(
    val symbols: List<SymbolSummaryResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun from(symbols: Page<Symbol>): SymbolPageResult =
            SymbolPageResult(
                symbols = symbols.content.map(SymbolSummaryResult::from),
                page = symbols.number,
                size = symbols.size,
                totalElements = symbols.totalElements,
                totalPages = symbols.totalPages,
            )
    }
}
