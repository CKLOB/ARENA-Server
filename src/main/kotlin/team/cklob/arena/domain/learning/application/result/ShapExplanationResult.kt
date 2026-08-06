package team.cklob.arena.domain.learning.application.result

import java.math.BigDecimal

data class ShapExplanationResult(
    val decisionId: Long,
    val baseValue: BigDecimal,
    val contributions: List<ShapContributionResult>,
    val finalProbability: BigDecimal,
)
