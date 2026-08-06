package team.cklob.arena.domain.learning.infrastructure

import org.springframework.http.HttpStatus
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import team.cklob.arena.domain.learning.LearningErrorCode
import team.cklob.arena.domain.learning.application.ObservabilityClient
import team.cklob.arena.domain.learning.application.result.RetrainingJobResult
import team.cklob.arena.domain.learning.application.result.ShapContributionResult
import team.cklob.arena.domain.learning.application.result.ShapExplanationResult
import team.cklob.arena.domain.learning.infrastructure.dto.RetrainingJobDto
import team.cklob.arena.domain.learning.infrastructure.dto.ShapExplanationDto
import team.cklob.arena.domain.learning.infrastructure.property.ObservabilityProperties
import team.cklob.arena.global.exception.ExpectedException
import java.net.http.HttpClient

@Component
class ObservabilityHttpClient(
    restClientBuilder: RestClient.Builder,
    private val properties: ObservabilityProperties,
) : ObservabilityClient {
    private val requestFactory =
        JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(properties.connectTimeout).build(),
        ).apply { setReadTimeout(properties.readTimeout) }
    private val aiClient =
        restClientBuilder.clone()
            .requestFactory(requestFactory)
            .baseUrl(properties.aiBaseUrl)
            .build()
    private val n8nClient =
        restClientBuilder.clone()
            .requestFactory(requestFactory)
            .build()

    override fun requestRetraining(): RetrainingJobResult =
        executeRequest {
            val response =
                n8nClient.post()
                    .uri(requireValue(properties.n8nRetrainUrl))
                    .header(INTERNAL_API_KEY_HEADER, requireValue(properties.n8nApiKey))
                    .retrieve()
                    .onStatus({ status -> status != HttpStatus.ACCEPTED }) { _, _ -> unavailable() }
                    .body(RetrainingJobDto::class.java)
                    ?: unavailable()
            RetrainingJobResult(response.jobId.takeIf(String::isNotBlank) ?: unavailable())
        }

    override fun getShapExplanation(decisionId: Long): ShapExplanationResult =
        executeRequest {
            val response =
                aiClient.get()
                    .uri("/internal/ai/decisions/{decisionId}/shap", decisionId)
                    .header(INTERNAL_API_KEY_HEADER, requireValue(properties.aiApiKey))
                    .retrieve()
                    .body(ShapExplanationDto::class.java)
                    ?: unavailable()
            if (response.decisionId != decisionId) unavailable()
            ShapExplanationResult(
                response.decisionId,
                response.baseValue,
                response.contributions.map { ShapContributionResult(it.featureName, it.contribution) },
                response.finalProbability,
            )
        }

    private fun requireValue(value: String): String = value.takeIf(String::isNotBlank) ?: unavailable()

    private fun <T> executeRequest(block: () -> T): T =
        try {
            block()
        } catch (exception: ExpectedException) {
            throw exception
        } catch (exception: RestClientException) {
            unavailable()
        } catch (exception: IllegalArgumentException) {
            unavailable()
        }

    private fun unavailable(): Nothing = throw ExpectedException(LearningErrorCode.EXTERNAL_SERVICE_UNAVAILABLE)

    private companion object {
        const val INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key"
    }
}
