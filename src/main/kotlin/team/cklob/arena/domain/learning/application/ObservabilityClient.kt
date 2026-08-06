package team.cklob.arena.domain.learning.application

import team.cklob.arena.domain.learning.application.result.RetrainingJobResult
import team.cklob.arena.domain.learning.application.result.ShapExplanationResult

interface ObservabilityClient {
    fun requestRetraining(): RetrainingJobResult

    fun getShapExplanation(decisionId: Long): ShapExplanationResult
}
