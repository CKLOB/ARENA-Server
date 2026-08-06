package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.ShapContributionResult
import java.math.BigDecimal

data class ShapContributionResponse(
    val featureName: String,
    val contribution: BigDecimal,
) {
    companion object {
        fun from(result: ShapContributionResult): ShapContributionResponse =
            ShapContributionResponse(result.featureName, result.contribution)
    }
}
