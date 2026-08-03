package team.cklob.arena.domain.market.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.market.application.result.SymbolSummaryResult

data class SymbolSummaryResponse(
    @field:Schema(example = "1")
    val symbolId: Long,
    @field:Schema(example = "AAPL")
    val code: String,
    @field:Schema(example = "Apple Inc")
    val name: String,
    @field:Schema(example = "true")
    val isActive: Boolean,
) {
    companion object {
        fun from(result: SymbolSummaryResult): SymbolSummaryResponse =
            SymbolSummaryResponse(result.symbolId, result.code, result.name, result.isActive)
    }
}
