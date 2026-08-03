package team.cklob.arena.domain.market.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.market.application.result.SymbolPageResult

data class SymbolPageResponse(
    val symbols: List<SymbolSummaryResponse>,
    @field:Schema(example = "0")
    val page: Int,
    @field:Schema(example = "50")
    val size: Int,
    @field:Schema(example = "120")
    val totalElements: Long,
    @field:Schema(example = "3")
    val totalPages: Int,
) {
    companion object {
        fun from(result: SymbolPageResult): SymbolPageResponse =
            SymbolPageResponse(
                symbols = result.symbols.map(SymbolSummaryResponse::from),
                page = result.page,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
            )
    }
}
