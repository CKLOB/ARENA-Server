package team.cklob.arena.domain.market.application.impl

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.market.MarketErrorCode
import team.cklob.arena.domain.market.application.GetSymbolsService
import team.cklob.arena.domain.market.application.result.SymbolPageResult
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import team.cklob.arena.global.exception.ExpectedException

@Service
class GetSymbolsServiceImpl(
    private val symbolRepository: SymbolRepository,
    private val marketProperties: MarketProperties,
) : GetSymbolsService {
    @Transactional(readOnly = true)
    override fun execute(
        market: MarketType,
        page: Int,
        size: Int,
    ): SymbolPageResult {
        requireActiveMarket(market)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"))
        return SymbolPageResult.from(symbolRepository.findAllByMarketAndIsActiveTrue(market, pageable))
    }

    private fun requireActiveMarket(market: MarketType) {
        if (market !in marketProperties.activeMarkets) throw ExpectedException(MarketErrorCode.MARKET_NOT_ACTIVE)
    }
}
