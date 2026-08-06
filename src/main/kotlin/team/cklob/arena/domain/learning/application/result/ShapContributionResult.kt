package team.cklob.arena.domain.learning.application.result

import java.math.BigDecimal

data class ShapContributionResult(
    val featureName: String,
    val contribution: BigDecimal,
)
