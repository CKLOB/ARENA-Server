package team.cklob.arena.domain.market.infrastructure.dto

data class TwelveDataTimeSeriesResponse(
    val values: List<TwelveDataTimeSeriesValue>?,
    val status: String?,
    val code: Int?,
    val message: String?,
)
