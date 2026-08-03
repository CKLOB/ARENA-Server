package team.cklob.arena.domain.market.infrastructure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "external.twelve-data")
data class TwelveDataProperties(
    val enabled: Boolean = false,
    val baseUrl: String = "https://api.twelvedata.com",
    val apiKey: String = "",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(4),
)
