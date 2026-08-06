package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.PromotedModelResult
import team.cklob.arena.domain.learning.domain.type.ModelStatus

data class PromoteModelResponse(
    val modelVersionId: Long,
    val status: ModelStatus,
) {
    companion object {
        fun from(result: PromotedModelResult): PromoteModelResponse = PromoteModelResponse(result.modelVersionId, result.status)
    }
}
