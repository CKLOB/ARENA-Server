package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.DecisionLogResult

data class DecisionLogsResponse(
    val decisions: List<DecisionLogResponse>,
) {
    companion object {
        fun from(results: List<DecisionLogResult>): DecisionLogsResponse = DecisionLogsResponse(results.map(DecisionLogResponse::from))
    }
}
