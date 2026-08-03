package team.cklob.arena.domain.market.application

import team.cklob.arena.domain.market.application.result.PriceHistoryResult
import java.time.LocalDate

interface GetPriceHistoryService {
    fun execute(
        symbolId: Long,
        from: LocalDate,
        to: LocalDate,
    ): PriceHistoryResult
}
