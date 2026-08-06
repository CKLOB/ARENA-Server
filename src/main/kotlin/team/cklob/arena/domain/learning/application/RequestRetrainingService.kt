package team.cklob.arena.domain.learning.application

import team.cklob.arena.domain.learning.application.result.RetrainingJobResult

interface RequestRetrainingService {
    fun execute(): RetrainingJobResult
}
