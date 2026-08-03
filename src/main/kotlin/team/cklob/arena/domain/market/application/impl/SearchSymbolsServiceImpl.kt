package team.cklob.arena.domain.market.application.impl

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.market.application.MarketValidator
import team.cklob.arena.domain.market.application.SearchSymbolsService
import team.cklob.arena.domain.market.application.result.SymbolPageResult
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.global.exception.CommonErrorCode
import team.cklob.arena.global.exception.ExpectedException

@Service
class SearchSymbolsServiceImpl(
    private val symbolRepository: SymbolRepository,
    private val marketValidator: MarketValidator,
) : SearchSymbolsService {
    @Transactional(readOnly = true)
    override fun execute(
        market: MarketType,
        keyword: String,
        page: Int,
        size: Int,
    ): SymbolPageResult {
        marketValidator.requireActiveMarket(market)
        val normalizedKeyword =
            keyword.trim().takeIf { it.isNotEmpty() && it.length <= 100 }
                ?: throw ExpectedException(CommonErrorCode.INVALID_REQUEST)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"))
        return SymbolPageResult.from(symbolRepository.searchActive(market, normalizedKeyword.escapeLikePattern(), pageable))
    }

    private fun String.escapeLikePattern(): String =
        replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
}
