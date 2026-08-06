package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.ShapExplanationResult
import java.math.BigDecimal

data class ShapExplanationResponse(
    val decisionId: Long,
    val baseValue: BigDecimal,
    val contributions: List<ShapContributionResponse>,
    val finalProbability: BigDecimal,
) {
    companion object {
        fun from(result: ShapExplanationResult): ShapExplanationResponse =
            ShapExplanationResponse(
                result.decisionId,
                result.baseValue,
                result.contributions.map(ShapContributionResponse::from),
                result.finalProbability,
            )
    }
}
