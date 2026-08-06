package team.cklob.arena.domain.learning.application.result

import team.cklob.arena.domain.learning.domain.type.ModelStatus

data class PromotedModelResult(
    val modelVersionId: Long,
    val status: ModelStatus,
)
