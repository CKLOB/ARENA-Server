package team.cklob.arena.domain.learning.application.result

import com.fasterxml.jackson.databind.JsonNode
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import java.time.LocalDateTime

data class ModelVersionResult(
    val modelVersionId: Long,
    val versionTag: String,
    val trainedAt: LocalDateTime,
    val performanceMetrics: JsonNode,
    val status: ModelStatus,
)
