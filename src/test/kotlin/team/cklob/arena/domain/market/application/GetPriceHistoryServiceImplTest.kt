package team.cklob.arena.domain.market.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.impl.GetPriceHistoryServiceImpl
import team.cklob.arena.domain.market.application.result.PricePointResult
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

class GetPriceHistoryServiceImplTest : DescribeSpec({
    val symbolRepository = mockk<SymbolRepository>()
    val cache = mockk<MarketDataCache>(relaxed = true)
    val client = mockk<MarketDataClient>()
    val service = GetPriceHistoryServiceImpl(symbolRepository, cache, client, MarketProperties())
    val symbol = Symbol(MarketType.COIN, "BTC/USD", "Bitcoin").apply { id = 1L }
    val from = LocalDate.of(2026, 7, 27)
    val to = LocalDate.of(2026, 8, 2)

    beforeEach {
        every { symbolRepository.findById(1L) } returns Optional.of(symbol)
    }

    it("7일을 초과한 조회를 외부 호출 전에 거절한다") {
        val exception = shouldThrow<ExpectedException> { service.execute(1L, from.minusDays(1), to) }

        exception.errorCode shouldBe MarketErrorCode.INVALID_PRICE_HISTORY_RANGE
        verify(exactly = 0) { symbolRepository.findById(any()) }
    }

    it("외부 히스토리를 시간 오름차순으로 캐시한다") {
        every { cache.findPriceHistory(1L, from.toString(), to.toString()) } returns null
        every { client.fetchPriceHistory("BTC/USD", from, to) } returns
            listOf(
                PricePointResult(BigDecimal("120"), Instant.parse("2026-08-02T10:00:00Z")),
                PricePointResult(BigDecimal("110"), Instant.parse("2026-08-02T09:00:00Z")),
            )

        val result = service.execute(1L, from, to)

        result.prices.shouldBeSortedBy(PricePointResult::snapshotAt)
        verify(exactly = 1) { cache.savePriceHistory(result) }
    }
})
