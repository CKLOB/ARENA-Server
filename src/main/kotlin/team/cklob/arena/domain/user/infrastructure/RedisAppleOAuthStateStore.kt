package team.cklob.arena.domain.user.infrastructure

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import team.cklob.arena.domain.user.infrastructure.dto.AppleOAuthState
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64

@Component
class RedisAppleOAuthStateStore(
    private val redisTemplate: StringRedisTemplate,
) : AppleOAuthStateStore {
    override fun create(): AppleOAuthState {
        val state = randomValue()
        val verifier = randomValue()
        redisTemplate.opsForValue().set(key(state), verifier, STATE_TTL)
        return AppleOAuthState(state, challenge(verifier))
    }

    override fun consume(state: String): String =
        redisTemplate.opsForValue().getAndDelete(key(state))
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)

    private fun randomValue(): String =
        ByteArray(32).also(random::nextBytes).let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }

    private fun challenge(verifier: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))

    private fun key(state: String) = "oauth:apple:pkce:$state"

    private companion object {
        val random = SecureRandom()
        val STATE_TTL = Duration.ofMinutes(5)
    }
}
