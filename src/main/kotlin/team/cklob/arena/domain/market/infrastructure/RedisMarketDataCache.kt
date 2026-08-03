package team.cklob.arena.domain.market.infrastructure

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import team.cklob.arena.domain.market.application.MarketDataCache
import team.cklob.arena.domain.market.application.result.CurrentPriceResult
import team.cklob.arena.domain.market.application.result.PriceHistoryResult
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import java.time.Duration

@Component
class RedisMarketDataCache(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val properties: MarketProperties,
) : MarketDataCache {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun findCurrentPrice(symbolId: Long): CurrentPriceResult? = read(priceKey(symbolId), CurrentPriceResult::class.java)

    override fun saveCurrentPrice(
        market: MarketType,
        result: CurrentPriceResult,
    ) {
        write(priceKey(result.symbolId), result, properties.quoteTtl[market] ?: Duration.ofSeconds(60))
    }

    override fun findPriceHistory(
        symbolId: Long,
        from: String,
        to: String,
    ): PriceHistoryResult? = read(historyKey(symbolId, from, to), PriceHistoryResult::class.java)

    override fun savePriceHistory(result: PriceHistoryResult) {
        write(historyKey(result.symbolId, result.from, result.to), result, properties.historyTtl)
    }

    private fun <T> read(
        key: String,
        type: Class<T>,
    ): T? =
        try {
            redisTemplate.opsForValue().get(key)?.let { objectMapper.readValue(it, type) }
        } catch (exception: DataAccessException) {
            log.warn("Market cache read failed key={}", key, exception)
            null
        } catch (exception: JsonProcessingException) {
            log.warn("Market cache value is invalid key={}", key, exception)
            null
        }

    private fun write(
        key: String,
        value: Any,
        ttl: Duration,
    ) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl)
        } catch (exception: DataAccessException) {
            log.warn("Market cache write failed key={}", key, exception)
        } catch (exception: JsonProcessingException) {
            log.warn("Market cache serialization failed key={}", key, exception)
        }
    }

    private fun priceKey(symbolId: Long) = "market:price:$symbolId"

    private fun historyKey(
        symbolId: Long,
        from: String,
        to: String,
    ) = "market:history:$symbolId:$from:$to:1h"
}
