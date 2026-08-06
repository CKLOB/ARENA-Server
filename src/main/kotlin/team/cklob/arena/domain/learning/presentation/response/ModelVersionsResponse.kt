package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.ModelVersionResult

data class ModelVersionsResponse(
    val models: List<ModelVersionResponse>,
) {
    companion object {
        fun from(results: List<ModelVersionResult>): ModelVersionsResponse = ModelVersionsResponse(results.map(ModelVersionResponse::from))
    }
}
