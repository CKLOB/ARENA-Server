package team.cklob.arena.domain.market.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.impl.SyncSymbolsServiceImpl
import team.cklob.arena.domain.market.application.result.ExternalSymbolResult
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException
import java.util.function.Consumer

class SyncSymbolsServiceImplTest : DescribeSpec({
    val client = mockk<MarketDataClient>()
    val repository = mockk<SymbolRepository>(relaxed = true)
    val transactionTemplate = mockk<TransactionTemplate>()
    val service =
        SyncSymbolsServiceImpl(
            client,
            repository,
            transactionTemplate,
            MarketProperties(symbolSyncMaxDeactivationRatio = 1.0),
        )

    beforeEach {
        clearMocks(client, repository, transactionTemplate)
        every { transactionTemplate.executeWithoutResult(any()) } answers {
            firstArg<Consumer<TransactionStatus>>().accept(mockk())
        }
        every { repository.save(any<Symbol>()) } answers { firstArg() }
    }

    it("종목을 갱신하고 공급자에서 사라진 종목을 비활성화한다") {
        val apple = Symbol(MarketType.US, "AAPL", "Old Apple")
        val removed = Symbol(MarketType.US, "OLD", "Removed")
        every { client.fetchSymbols() } returns
            listOf(
                ExternalSymbolResult(MarketType.US, "aapl", "Apple Inc"),
                ExternalSymbolResult(MarketType.COIN, "btc/usd", "Bitcoin"),
            )
        every { repository.findAllByMarket(MarketType.US) } returns listOf(apple, removed)
        every { repository.findAllByMarket(MarketType.COIN) } returns emptyList()

        service.execute()

        apple.name shouldBe "Apple Inc"
        apple.isActive shouldBe true
        removed.isActive shouldBe false
        verify(exactly = 1) { repository.save(match { it.market == MarketType.COIN && it.code == "BTC/USD" }) }
    }

    it("한 시장의 응답이 비어 있으면 DB 트랜잭션을 시작하지 않는다") {
        every { client.fetchSymbols() } returns listOf(ExternalSymbolResult(MarketType.US, "AAPL", "Apple Inc"))

        runCatching(service::execute)

        verify(exactly = 0) { transactionTemplate.executeWithoutResult(any()) }
    }

    it("불완전한 응답이 활성 종목의 허용 비율을 초과해 누락시키면 동기화를 거절한다") {
        val apple = Symbol(MarketType.US, "AAPL", "Apple Inc")
        val microsoft = Symbol(MarketType.US, "MSFT", "Microsoft Corporation")
        val bitcoin = Symbol(MarketType.COIN, "BTC/USD", "Bitcoin")
        val ethereum = Symbol(MarketType.COIN, "ETH/USD", "Ethereum")
        every { client.fetchSymbols() } returns
            listOf(
                ExternalSymbolResult(MarketType.US, "AAPL", "Apple Inc"),
                ExternalSymbolResult(MarketType.COIN, "BTC/USD", "Bitcoin"),
            )
        every { repository.findAllByMarket(MarketType.US) } returns listOf(apple, microsoft)
        every { repository.findAllByMarket(MarketType.COIN) } returns listOf(bitcoin, ethereum)
        val guardedService = SyncSymbolsServiceImpl(client, repository, transactionTemplate, MarketProperties())

        shouldThrow<ExpectedException> { guardedService.execute() }.errorCode shouldBe MarketErrorCode.SYMBOL_SYNC_REJECTED

        microsoft.isActive shouldBe true
        ethereum.isActive shouldBe true
        verify(exactly = 0) { repository.save(any<Symbol>()) }
    }
})
