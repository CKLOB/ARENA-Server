package team.cklob.arena.domain.learning.application.impl

import org.springframework.stereotype.Service
import team.cklob.arena.domain.learning.application.ObservabilityClient
import team.cklob.arena.domain.learning.application.RequestRetrainingService
import team.cklob.arena.domain.learning.application.result.RetrainingJobResult

@Service
class RequestRetrainingServiceImpl(
    private val observabilityClient: ObservabilityClient,
) : RequestRetrainingService {
    override fun execute(): RetrainingJobResult = observabilityClient.requestRetraining()
}
