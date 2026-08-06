package team.cklob.arena.domain.learning.infrastructure

import com.sun.net.httpserver.HttpServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.web.client.RestClient
import team.cklob.arena.domain.learning.LearningErrorCode
import team.cklob.arena.domain.learning.infrastructure.property.ObservabilityProperties
import team.cklob.arena.global.exception.ExpectedException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

class ObservabilityHttpClientTest : DescribeSpec({
    val responses = ConcurrentHashMap<String, Pair<Int, String>>()
    val apiKeys = ConcurrentHashMap<String, String?>()
    val server = HttpServer.create(InetSocketAddress(0), 0)
    server.createContext("/") { exchange ->
        apiKeys[exchange.requestURI.path] = exchange.requestHeaders.getFirst("X-Internal-Api-Key")
        val (status, body) = responses[exchange.requestURI.path] ?: (500 to "{}")
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
    server.start()
    val baseUrl = "http://localhost:${server.address.port}"
    val client =
        ObservabilityHttpClient(
            RestClient.builder(),
            ObservabilityProperties(
                aiBaseUrl = baseUrl,
                aiApiKey = "ai-key",
                n8nRetrainUrl = "$baseUrl/retrain",
                n8nApiKey = "n8n-key",
            ),
        )

    afterSpec { server.stop(0) }

    beforeEach {
        responses.clear()
        apiKeys.clear()
    }

    it("n8n 재학습 요청의 jobId를 반환한다") {
        responses["/retrain"] = 202 to "{\"jobId\":\"job-1\"}"

        client.requestRetraining().jobId shouldBe "job-1"
        apiKeys["/retrain"] shouldBe "n8n-key"
    }

    it("FastAPI SHAP 응답을 내부 결과로 변환한다") {
        responses["/internal/ai/decisions/1/shap"] =
            200 to
            """{"decisionId":1,"baseValue":0.4,"contributions":[{"featureName":"rsi","contribution":0.1}],"finalProbability":0.5}"""

        val result = client.getShapExplanation(1L)

        result.contributions.single().featureName shouldBe "rsi"
        apiKeys["/internal/ai/decisions/1/shap"] shouldBe "ai-key"
    }

    it("외부 오류를 도메인 오류로 변환한다") {
        responses["/retrain"] = 500 to "{}"

        shouldThrow<ExpectedException> { client.requestRetraining() }.errorCode shouldBe
            LearningErrorCode.EXTERNAL_SERVICE_UNAVAILABLE
    }

    it("n8n이 202 이외 상태를 반환하면 실패로 처리한다") {
        responses["/retrain"] = 200 to "{\"jobId\":\"job-1\"}"

        shouldThrow<ExpectedException> { client.requestRetraining() }.errorCode shouldBe
            LearningErrorCode.EXTERNAL_SERVICE_UNAVAILABLE
    }
})
