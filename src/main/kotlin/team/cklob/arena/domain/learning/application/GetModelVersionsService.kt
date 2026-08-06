package team.cklob.arena.domain.learning.application

import team.cklob.arena.domain.learning.application.result.ModelVersionResult
import team.cklob.arena.domain.learning.domain.type.ModelStatus

interface GetModelVersionsService {
    fun execute(status: ModelStatus?): List<ModelVersionResult>
}
