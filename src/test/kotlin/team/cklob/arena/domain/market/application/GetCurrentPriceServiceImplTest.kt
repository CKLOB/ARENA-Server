package team.cklob.arena.domain.market.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.impl.GetCurrentPriceServiceImpl
import team.cklob.arena.domain.market.application.result.CurrentPriceResult
import team.cklob.arena.domain.market.application.result.PriceQuoteResult
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

class GetCurrentPriceServiceImplTest : DescribeSpec({
    val symbolRepository = mockk<SymbolRepository>()
    val cache = mockk<MarketDataCache>(relaxed = true)
    val client = mockk<MarketDataClient>()
    val service = GetCurrentPriceServiceImpl(symbolRepository, cache, client, MarketProperties())
    val symbol = Symbol(MarketType.US, "AAPL", "Apple Inc").apply { id = 1L }
    val cached = CurrentPriceResult(1L, BigDecimal("220.10"), Instant.parse("2026-08-02T09:00:00Z"))

    beforeEach {
        every { symbolRepository.findById(1L) } returns Optional.of(symbol)
    }

    it("캐시된 현재가가 있으면 외부 API를 호출하지 않는다") {
        every { cache.findCurrentPrice(1L) } returns cached

        service.execute(1L) shouldBe cached

        verify(exactly = 0) { client.fetchCurrentPrice(any()) }
    }

    it("캐시 miss이면 외부 현재가를 조회해 캐시에 저장한다") {
        val quote = PriceQuoteResult(BigDecimal("221.20"), Instant.parse("2026-08-02T09:01:00Z"))
        every { cache.findCurrentPrice(1L) } returns null
        every { client.fetchCurrentPrice("AAPL") } returns quote

        val result = service.execute(1L)

        result.price shouldBe quote.price
        verify(exactly = 1) { cache.saveCurrentPrice(MarketType.US, result) }
    }

    it("비활성 시장의 현재가 요청을 거절한다") {
        val krSymbol = Symbol(MarketType.KR, "005930", "삼성전자").apply { id = 2L }
        every { symbolRepository.findById(2L) } returns Optional.of(krSymbol)

        shouldThrow<ExpectedException> { service.execute(2L) }.errorCode shouldBe MarketErrorCode.MARKET_NOT_ACTIVE
    }
})
