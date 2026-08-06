package team.cklob.arena.domain.learning.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.learning.application.GetDecisionLogsService
import team.cklob.arena.domain.learning.application.result.DecisionLogResult
import team.cklob.arena.domain.learning.domain.repository.DecisionLogRepository
import team.cklob.arena.domain.market.domain.type.MarketType

@Service
class GetDecisionLogsServiceImpl(
    private val decisionLogRepository: DecisionLogRepository,
) : GetDecisionLogsService {
    @Transactional(readOnly = true)
    override fun execute(
        challengeId: Long?,
        market: MarketType?,
        modelVersion: String?,
    ): List<DecisionLogResult> =
        decisionLogRepository.findAllByFilters(challengeId, market, modelVersion?.trim()?.takeIf(String::isNotEmpty))
            .map(DecisionLogResult::from)
}
