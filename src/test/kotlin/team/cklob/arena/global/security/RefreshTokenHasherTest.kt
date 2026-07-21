package team.cklob.arena.global.security

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RefreshTokenHasherTest : DescribeSpec({
    it("같은 refresh token은 같은 SHA-256 hash를 만든다") {
        RefreshTokenHasher.hash("refresh-token") shouldBe RefreshTokenHasher.hash("refresh-token")
    }
})
