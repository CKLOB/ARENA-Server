package team.cklob.arena.domain.market.infrastructure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import team.cklob.arena.domain.market.domain.type.MarketType
import java.time.Duration

@ConfigurationProperties(prefix = "market")
data class MarketProperties(
    val activeMarkets: Set<MarketType> = setOf(MarketType.US, MarketType.COIN),
    val quoteTtl: Map<MarketType, Duration> =
        mapOf(
            MarketType.US to Duration.ofSeconds(60),
            MarketType.COIN to Duration.ofSeconds(15),
        ),
    val historyTtl: Duration = Duration.ofMinutes(15),
    val historyMaxDays: Long = 7,
    val symbolSyncMaxDeactivationRatio: Double = 0.2,
)
