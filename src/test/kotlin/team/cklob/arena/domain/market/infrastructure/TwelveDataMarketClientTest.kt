package team.cklob.arena.domain.market.infrastructure

import com.sun.net.httpserver.HttpServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.web.client.RestClient
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.TwelveDataProperties
import team.cklob.arena.global.exception.ExpectedException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

class TwelveDataMarketClientTest : DescribeSpec({
    val responses = ConcurrentHashMap<String, String>()
    val server = HttpServer.create(InetSocketAddress(0), 0)
    server.createContext("/") { exchange ->
        val body = responses[exchange.requestURI.path] ?: "{\"status\":\"error\"}"
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
    server.start()
    val client =
        TwelveDataMarketClient(
            RestClient.builder(),
            TwelveDataProperties(baseUrl = "http://localhost:${server.address.port}", apiKey = "test-key"),
        )

    afterSpec { server.stop(0) }

    beforeEach {
        responses.clear()
    }

    it("미국 보통주와 USD 암호화폐 종목을 내부 결과로 변환한다") {
        responses["/stocks"] =
            """{"data":[{"symbol":"AAPL","name":"Apple Inc","country":"United States","type":"Common Stock"}]}"""
        responses["/cryptocurrencies"] =
            """
            {"data":[
              {"symbol":"BTC/USD","currency_base":"Bitcoin","currency_quote":"US Dollar"},
              {"symbol":"ETH/BTC","currency_base":"Ethereum","currency_quote":"Bitcoin"}
            ]}
            """.trimIndent()

        val symbols = client.fetchSymbols()

        symbols shouldHaveSize 2
        symbols.first { it.market == MarketType.COIN }.code shouldBe "BTC/USD"
    }

    it("현재가와 UTC 1시간봉을 변환한다") {
        responses["/quote"] = """{"close":"221.25","timestamp":1785661200}"""
        responses["/time_series"] =
            """{"values":[{"datetime":"2026-08-02 09:00:00","close":"220.10"},{"datetime":"2026-08-02 10:00:00","close":"221.25"}]}"""

        client.fetchCurrentPrice("AAPL").price.toPlainString() shouldBe "221.25"
        client.fetchPriceHistory("AAPL", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)) shouldHaveSize 2
    }

    it("공급자 오류 응답을 503 도메인 오류로 변환한다") {
        responses["/quote"] = """{"status":"error","code":429,"message":"rate limit"}"""

        shouldThrow<ExpectedException> { client.fetchCurrentPrice("AAPL") }.errorCode shouldBe
            MarketErrorCode.MARKET_DATA_UNAVAILABLE
    }
})
