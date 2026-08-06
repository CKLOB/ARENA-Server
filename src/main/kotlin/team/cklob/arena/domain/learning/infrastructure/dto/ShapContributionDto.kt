package team.cklob.arena.domain.learning.infrastructure.dto

import java.math.BigDecimal

data class ShapContributionDto(
    val featureName: String,
    val contribution: BigDecimal,
)
