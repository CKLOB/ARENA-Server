package team.cklob.arena.domain.market.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TwelveDataSymbol(
    val symbol: String?,
    val name: String?,
    val country: String?,
    val type: String?,
    @field:JsonProperty("currency_base")
    val currencyBase: String?,
    @field:JsonProperty("currency_quote")
    val currencyQuote: String?,
)
