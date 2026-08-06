package team.cklob.arena.domain.learning.infrastructure.dto

import java.math.BigDecimal

data class ShapExplanationDto(
    val decisionId: Long,
    val baseValue: BigDecimal,
    val contributions: List<ShapContributionDto>,
    val finalProbability: BigDecimal,
)
