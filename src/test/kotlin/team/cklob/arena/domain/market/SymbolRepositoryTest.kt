package team.cklob.arena.domain.market

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType

@SpringBootTest
class SymbolRepositoryTest(
    private val symbolRepository: SymbolRepository,
) : DescribeSpec({
        extension(SpringExtension)

        beforeEach {
            symbolRepository.deleteAll()
            symbolRepository.saveAll(
                listOf(
                    Symbol(MarketType.US, "MSFT", "Microsoft Corporation"),
                    Symbol(MarketType.US, "AAPL", "Apple Inc"),
                    Symbol(MarketType.US, "OLD", "Old Inc", isActive = false),
                    Symbol(MarketType.COIN, "BTC/USD", "Bitcoin"),
                ),
            )
        }

        it("활성 종목만 코드순으로 페이지 조회한다") {
            val result =
                symbolRepository.findAllByMarketAndIsActiveTrue(
                    MarketType.US,
                    PageRequest.of(0, 10, Sort.by("code")),
                )

            result.content.map(Symbol::code) shouldBe listOf("AAPL", "MSFT")
        }

        it("코드와 이름을 대소문자 구분 없이 검색한다") {
            val result = symbolRepository.searchActive(MarketType.US, "micro", PageRequest.of(0, 10))

            result.content.map(Symbol::code) shouldBe listOf("MSFT")
        }

        it("LIKE 와일드카드를 일반 문자로 검색한다") {
            symbolRepository.save(Symbol(MarketType.US, "A_PL", "Literal underscore"))

            val result = symbolRepository.searchActive(MarketType.US, "A\\_PL", PageRequest.of(0, 10))

            result.content.map(Symbol::code) shouldBe listOf("A_PL")
        }
    }) {
    override fun extensions() = listOf(SpringExtension)
}
