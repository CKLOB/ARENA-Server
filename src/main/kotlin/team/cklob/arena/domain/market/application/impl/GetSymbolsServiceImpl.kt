package team.cklob.arena.domain.market.application.impl

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.market.application.GetSymbolsService
import team.cklob.arena.domain.market.application.MarketValidator
import team.cklob.arena.domain.market.application.result.SymbolPageResult
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType

@Service
class GetSymbolsServiceImpl(
    private val symbolRepository: SymbolRepository,
    private val marketValidator: MarketValidator,
) : GetSymbolsService {
    @Transactional(readOnly = true)
    override fun execute(
        market: MarketType,
        page: Int,
        size: Int,
    ): SymbolPageResult {
        marketValidator.requireActiveMarket(market)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"))
        return SymbolPageResult.from(symbolRepository.findAllByMarketAndIsActiveTrue(market, pageable))
    }
}
