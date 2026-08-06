package team.cklob.arena.domain.learning.application

import team.cklob.arena.domain.learning.application.result.ShapExplanationResult

interface GetShapExplanationService {
    fun execute(decisionId: Long): ShapExplanationResult
}
