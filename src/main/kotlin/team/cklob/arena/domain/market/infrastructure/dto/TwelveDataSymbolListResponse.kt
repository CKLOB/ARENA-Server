package team.cklob.arena.domain.market.infrastructure.dto

data class TwelveDataSymbolListResponse(
    val data: List<TwelveDataSymbol>?,
    val status: String?,
    val code: Int?,
    val message: String?,
)
