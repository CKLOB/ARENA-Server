package team.cklob.arena.domain.user.infrastructure

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class RedisRefreshTokenStore(
    private val redisTemplate: StringRedisTemplate,
) : RefreshTokenStore {
    override fun save(
        sessionId: Long,
        tokenHash: String,
        expiresAt: LocalDateTime,
    ) {
        val ttl = Duration.between(LocalDateTime.now(), expiresAt)
        if (!ttl.isNegative && !ttl.isZero) {
            redisTemplate.opsForValue().set(key(sessionId), tokenHash, ttl)
        }
    }

    override fun findTokenHash(sessionId: Long): String? = redisTemplate.opsForValue().get(key(sessionId))

    override fun delete(sessionId: Long) {
        redisTemplate.delete(key(sessionId))
    }

    override fun deleteAll(sessionIds: Collection<Long>) {
        redisTemplate.delete(sessionIds.map(::key))
    }

    private fun key(sessionId: Long) = "auth:refresh:$sessionId"
}
