package team.cklob.arena.domain.user.infrastructure

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import team.cklob.arena.global.exception.ExpectedException
import java.security.MessageDigest
import java.time.Duration
import java.util.Base64

class AppleOAuthStateServiceTest : DescribeSpec({
    val redisTemplate = mockk<StringRedisTemplate>()
    val values = mockk<ValueOperations<String, String>>()
    val service = RedisAppleOAuthStateStore(redisTemplate)

    beforeEach {
        every { redisTemplate.opsForValue() } returns values
    }

    it("state와 verifier를 TTL로 저장하고 S256 challenge를 만든다") {
        val key = slot<String>()
        val verifier = slot<String>()
        every { values.set(capture(key), capture(verifier), Duration.ofMinutes(5)) } returns Unit

        val result = service.create()

        key.captured shouldBe "oauth:apple:pkce:${result.state}"
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.captured.toByteArray())
        Base64.getUrlEncoder().withoutPadding().encodeToString(digest) shouldBe result.challenge
    }

    it("일치하는 state의 verifier를 한 번만 소비한다") {
        every { values.getAndDelete("oauth:apple:pkce:state") } returns "verifier"

        service.consume("state") shouldBe "verifier"
    }

    it("일치하지 않는 state를 거부한다") {
        every { values.getAndDelete("oauth:apple:pkce:wrong") } returns null

        (runCatching { service.consume("wrong") }.exceptionOrNull() is ExpectedException) shouldBe true
    }

    it("만료된 state를 거부한다") {
        every { values.getAndDelete("oauth:apple:pkce:expired") } returns null

        (runCatching { service.consume("expired") }.exceptionOrNull() is ExpectedException) shouldBe true
    }
})
