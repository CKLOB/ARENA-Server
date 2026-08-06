package team.cklob.arena.domain.learning.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.learning.LearningErrorCode
import team.cklob.arena.domain.learning.application.PromoteModelService
import team.cklob.arena.domain.learning.application.result.PromotedModelResult
import team.cklob.arena.domain.learning.domain.repository.ModelVersionRepository
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import team.cklob.arena.global.exception.ExpectedException

@Service
class PromoteModelServiceImpl(
    private val modelVersionRepository: ModelVersionRepository,
) : PromoteModelService {
    @Transactional
    override fun execute(modelVersionId: Long): PromotedModelResult {
        // ponytail: locks the small model registry; use a DB advisory lock if model volume becomes large.
        val models = modelVersionRepository.findAllForUpdate()
        val target =
            models.firstOrNull { it.id == modelVersionId }
                ?: throw ExpectedException(LearningErrorCode.MODEL_VERSION_NOT_FOUND)
        if (target.status != ModelStatus.CHALLENGER) throw ExpectedException(LearningErrorCode.MODEL_STATUS_CONFLICT)

        models.filter { it.status == ModelStatus.CHAMPION }.forEach { it.retire() }
        target.promote()
        return PromotedModelResult(requireNotNull(target.id), target.status)
    }
}
