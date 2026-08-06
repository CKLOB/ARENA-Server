package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.DecisionLogResult
import team.cklob.arena.domain.learning.domain.type.TradingAction
import team.cklob.arena.domain.market.domain.type.MarketType
import java.math.BigDecimal
import java.time.LocalDateTime

data class DecisionLogResponse(
    val decisionId: Long,
    val symbolCode: String,
    val market: MarketType,
    val action: TradingAction,
    val probability: BigDecimal,
    val modelVersion: String,
    val decidedAt: LocalDateTime,
) {
    companion object {
        fun from(result: DecisionLogResult): DecisionLogResponse =
            DecisionLogResponse(
                result.decisionId,
                result.symbolCode,
                result.market,
                result.action,
                result.probability,
                result.modelVersion,
                result.decidedAt,
            )
    }
}
