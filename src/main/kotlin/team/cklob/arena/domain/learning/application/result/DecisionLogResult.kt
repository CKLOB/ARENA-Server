package team.cklob.arena.domain.learning.application.result

import team.cklob.arena.domain.learning.domain.entity.DecisionLog
import team.cklob.arena.domain.learning.domain.type.TradingAction
import team.cklob.arena.domain.market.domain.type.MarketType
import java.math.BigDecimal
import java.time.LocalDateTime

data class DecisionLogResult(
    val decisionId: Long,
    val symbolCode: String,
    val market: MarketType,
    val action: TradingAction,
    val probability: BigDecimal,
    val modelVersion: String,
    val decidedAt: LocalDateTime,
) {
    companion object {
        fun from(decision: DecisionLog): DecisionLogResult =
            DecisionLogResult(
                decisionId = requireNotNull(decision.id),
                symbolCode = decision.symbolCode,
                market = decision.market,
                action = decision.action,
                probability = decision.probability,
                modelVersion = decision.modelVersion,
                decidedAt = decision.decidedAt,
            )
    }
}
