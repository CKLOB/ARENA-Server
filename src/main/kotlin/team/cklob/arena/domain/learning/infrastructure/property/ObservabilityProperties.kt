package team.cklob.arena.domain.learning.infrastructure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "external.observability")
data class ObservabilityProperties(
    val aiBaseUrl: String = "",
    val aiApiKey: String = "",
    val n8nRetrainUrl: String = "",
    val n8nApiKey: String = "",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(10),
)
