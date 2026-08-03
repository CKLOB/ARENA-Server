package team.cklob.arena.domain.market.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import team.cklob.arena.domain.market.application.result.CurrentPriceResult
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.infrastructure.property.MarketProperties
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

class RedisMarketDataCacheTest : DescribeSpec({
    val redisTemplate = mockk<StringRedisTemplate>()
    val valueOperations = mockk<ValueOperations<String, String>>(relaxed = true)
    val objectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
    val cache = RedisMarketDataCache(redisTemplate, objectMapper, MarketProperties())
    val result = CurrentPriceResult(1L, BigDecimal("221.25"), Instant.parse("2026-08-02T09:00:00Z"))

    beforeEach {
        every { redisTemplate.opsForValue() } returns valueOperations
    }

    it("COIN 현재가를 15초 TTL로 저장한다") {
        cache.saveCurrentPrice(MarketType.COIN, result)

        verify(exactly = 1) { valueOperations.set("market:price:1", any(), Duration.ofSeconds(15)) }
    }

    it("현재가 캐시 값을 역직렬화한다") {
        every { valueOperations.get("market:price:1") } returns objectMapper.writeValueAsString(result)

        cache.findCurrentPrice(1L) shouldBe result
    }
})
