package team.cklob.arena.domain.learning.application.impl

import org.springframework.stereotype.Service
import team.cklob.arena.domain.learning.LearningErrorCode
import team.cklob.arena.domain.learning.application.GetShapExplanationService
import team.cklob.arena.domain.learning.application.ObservabilityClient
import team.cklob.arena.domain.learning.application.result.ShapExplanationResult
import team.cklob.arena.domain.learning.domain.repository.DecisionLogRepository
import team.cklob.arena.global.exception.ExpectedException

@Service
class GetShapExplanationServiceImpl(
    private val decisionLogRepository: DecisionLogRepository,
    private val observabilityClient: ObservabilityClient,
) : GetShapExplanationService {
    override fun execute(decisionId: Long): ShapExplanationResult {
        if (!decisionLogRepository.existsById(decisionId)) throw ExpectedException(LearningErrorCode.DECISION_LOG_NOT_FOUND)
        return observabilityClient.getShapExplanation(decisionId)
    }
}
