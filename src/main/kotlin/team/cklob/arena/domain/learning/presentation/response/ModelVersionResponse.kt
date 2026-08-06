package team.cklob.arena.domain.learning.presentation.response

import com.fasterxml.jackson.databind.JsonNode
import team.cklob.arena.domain.learning.application.result.ModelVersionResult
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import java.time.LocalDateTime

data class ModelVersionResponse(
    val modelVersionId: Long,
    val versionTag: String,
    val trainedAt: LocalDateTime,
    val performanceMetrics: JsonNode,
    val status: ModelStatus,
) {
    companion object {
        fun from(result: ModelVersionResult): ModelVersionResponse =
            ModelVersionResponse(
                result.modelVersionId,
                result.versionTag,
                result.trainedAt,
                result.performanceMetrics,
                result.status,
            )
    }
}
