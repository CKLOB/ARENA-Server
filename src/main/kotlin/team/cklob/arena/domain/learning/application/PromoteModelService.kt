package team.cklob.arena.domain.learning.application

import team.cklob.arena.domain.learning.application.result.PromotedModelResult

interface PromoteModelService {
    fun execute(modelVersionId: Long): PromotedModelResult
}
