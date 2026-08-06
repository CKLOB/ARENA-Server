package team.cklob.arena.domain.learning.presentation.response

import team.cklob.arena.domain.learning.application.result.RetrainingJobResult

data class RetrainingResponse(
    val jobId: String,
) {
    companion object {
        fun from(result: RetrainingJobResult): RetrainingResponse = RetrainingResponse(result.jobId)
    }
}
