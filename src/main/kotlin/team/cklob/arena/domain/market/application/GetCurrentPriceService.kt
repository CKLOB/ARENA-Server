package team.cklob.arena.domain.market.application

import team.cklob.arena.domain.market.application.result.CurrentPriceResult

interface GetCurrentPriceService {
    fun execute(symbolId: Long): CurrentPriceResult
}
