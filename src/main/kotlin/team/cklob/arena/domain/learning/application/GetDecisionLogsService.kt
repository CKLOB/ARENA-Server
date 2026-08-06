package team.cklob.arena.domain.learning.application

import team.cklob.arena.domain.learning.application.result.DecisionLogResult
import team.cklob.arena.domain.market.domain.type.MarketType

interface GetDecisionLogsService {
    fun execute(
        challengeId: Long?,
        market: MarketType?,
        modelVersion: String?,
    ): List<DecisionLogResult>
}
