package team.cklob.arena.domain.market.infrastructure

import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.MarketDataClient
import team.cklob.arena.domain.market.application.result.ExternalSymbolResult
import team.cklob.arena.domain.market.application.result.PricePointResult
import team.cklob.arena.domain.market.application.result.PriceQuoteResult
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.dto.TwelveDataQuoteResponse
import team.cklob.arena.domain.market.infrastructure.dto.TwelveDataSymbol
import team.cklob.arena.domain.market.infrastructure.dto.TwelveDataSymbolListResponse
import team.cklob.arena.domain.market.infrastructure.dto.TwelveDataTimeSeriesResponse
import team.cklob.arena.domain.market.infrastructure.property.TwelveDataProperties
import team.cklob.arena.global.exception.ExpectedException
import java.net.http.HttpClient
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class TwelveDataMarketClient(
    restClientBuilder: RestClient.Builder,
    private val properties: TwelveDataProperties,
) : MarketDataClient {
    private val restClient =
        restClientBuilder
            .clone()
            .baseUrl(properties.baseUrl)
            .requestFactory(
                JdkClientHttpRequestFactory(
                    HttpClient.newBuilder().connectTimeout(properties.connectTimeout).build(),
                ).apply { setReadTimeout(properties.readTimeout) },
            ).build()

    override fun fetchSymbols(): List<ExternalSymbolResult> =
        executeRequest {
            val stocks =
                fetchSymbolList("/stocks", "country" to "United States", "type" to "Common Stock")
                    .mapNotNull { symbol ->
                        val code = symbol.symbol?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
                        val name = symbol.name?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
                        ExternalSymbolResult(MarketType.US, code, name)
                    }
            val coins =
                fetchSymbolList("/cryptocurrencies").mapNotNull { symbol ->
                    val code =
                        symbol.symbol?.trim()?.takeIf { it.endsWith("/USD", ignoreCase = true) }
                            ?: return@mapNotNull null
                    val name =
                        symbol.currencyBase?.trim()?.takeIf(String::isNotEmpty)
                            ?: symbol.name?.trim()?.takeIf(String::isNotEmpty)
                            ?: code.substringBefore('/')
                    ExternalSymbolResult(MarketType.COIN, code, name)
                }
            (stocks + coins).takeIf(List<ExternalSymbolResult>::isNotEmpty) ?: unavailable()
        }

    override fun fetchCurrentPrice(code: String): PriceQuoteResult =
        executeRequest {
            val response =
                restClient.get()
                    .uri { builder ->
                        builder.path("/quote")
                            .queryParam("symbol", code)
                            .queryParam("apikey", requireApiKey())
                            .build()
                    }.retrieve()
                    .body(TwelveDataQuoteResponse::class.java)
                    ?: unavailable()
            if (response.status == "error") unavailable()
            PriceQuoteResult(
                price = response.close?.toBigDecimalOrNull() ?: unavailable(),
                snapshotAt = response.timestamp?.let(Instant::ofEpochSecond) ?: unavailable(),
            )
        }

    override fun fetchPriceHistory(
        code: String,
        from: LocalDate,
        to: LocalDate,
    ): List<PricePointResult> =
        executeRequest {
            val response =
                restClient.get()
                    .uri { builder ->
                        builder.path("/time_series")
                            .queryParam("symbol", code)
                            .queryParam("interval", "1h")
                            .queryParam("start_date", from)
                            .queryParam("end_date", to)
                            .queryParam("timezone", "UTC")
                            .queryParam("order", "ASC")
                            .queryParam("apikey", requireApiKey())
                            .build()
                    }.retrieve()
                    .body(TwelveDataTimeSeriesResponse::class.java)
                    ?: unavailable()
            if (response.status == "error") unavailable()
            response.values.orEmpty().mapNotNull { value ->
                val price = value.close?.toBigDecimalOrNull() ?: return@mapNotNull null
                val snapshotAt = value.datetime?.let(::parseInstant) ?: return@mapNotNull null
                PricePointResult(price, snapshotAt)
            }.takeIf(List<PricePointResult>::isNotEmpty) ?: unavailable()
        }

    private fun fetchSymbolList(
        path: String,
        vararg filters: Pair<String, String>,
    ): List<TwelveDataSymbol> =
        restClient.get()
            .uri { builder ->
                builder.path(path).apply {
                    filters.forEach { (name, value) -> queryParam(name, value) }
                }.queryParam("apikey", requireApiKey()).build()
            }.retrieve()
            .body(TwelveDataSymbolListResponse::class.java)
            ?.takeUnless { it.status == "error" }
            ?.data
            ?.takeIf { it.isNotEmpty() }
            ?: unavailable()

    private fun requireApiKey(): String = properties.apiKey.takeIf(String::isNotBlank) ?: unavailable()

    private fun parseInstant(value: String): Instant = LocalDateTime.parse(value, DATE_TIME_FORMATTER).toInstant(ZoneOffset.UTC)

    private fun <T> executeRequest(block: () -> T): T =
        try {
            block()
        } catch (exception: ExpectedException) {
            throw exception
        } catch (exception: RestClientException) {
            unavailable()
        } catch (exception: IllegalArgumentException) {
            unavailable()
        }

    private fun unavailable(): Nothing = throw ExpectedException(MarketErrorCode.MARKET_DATA_UNAVAILABLE)

    private companion object {
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
