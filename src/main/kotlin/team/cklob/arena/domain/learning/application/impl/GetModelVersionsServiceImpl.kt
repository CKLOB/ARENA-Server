package team.cklob.arena.domain.learning.application.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.learning.application.GetModelVersionsService
import team.cklob.arena.domain.learning.application.result.ModelVersionResult
import team.cklob.arena.domain.learning.domain.repository.ModelVersionRepository
import team.cklob.arena.domain.learning.domain.type.ModelStatus

@Service
class GetModelVersionsServiceImpl(
    private val modelVersionRepository: ModelVersionRepository,
    private val objectMapper: ObjectMapper,
) : GetModelVersionsService {
    @Transactional(readOnly = true)
    override fun execute(status: ModelStatus?): List<ModelVersionResult> =
        (
            status?.let(modelVersionRepository::findAllByStatusOrderByTrainedAtDesc)
                ?: modelVersionRepository.findAllByOrderByTrainedAtDesc()
        ).map { model ->
            ModelVersionResult(
                modelVersionId = requireNotNull(model.id),
                versionTag = model.versionTag,
                trainedAt = model.trainedAt,
                performanceMetrics = objectMapper.readTree(model.performanceMetrics),
                status = model.status,
            )
        }
}
