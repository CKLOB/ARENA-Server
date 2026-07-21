package team.cklob.arena.domain.user.infrastructure

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.util.UriComponentsBuilder
import team.cklob.arena.domain.user.infrastructure.dto.AppleOAuthState
import team.cklob.arena.domain.user.infrastructure.property.AppleOAuthProperties
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties

class AppleOAuthAuthorizationServiceTest : DescribeSpec({
    it("Apple Android authorize URL에 state와 S256 challenge를 포함한다") {
        val stateStore = mockk<AppleOAuthStateStore>()
        every { stateStore.create() } returns AppleOAuthState("state", "challenge")
        val service =
            AppleOAuthAuthorizationServiceImpl(
                OAuthProperties(
                    apple =
                        AppleOAuthProperties(
                            androidClientId = "apple-services-id",
                            androidRedirectUri = "https://web.example.com/oauth/apple",
                        ),
                ),
                stateStore,
            )

        val query = UriComponentsBuilder.fromUriString(service.execute()).build().queryParams

        query.getFirst("client_id") shouldBe "apple-services-id"
        query.getFirst("redirect_uri") shouldBe "https://web.example.com/oauth/apple"
        query.getFirst("state") shouldBe "state"
        query.getFirst("code_challenge") shouldBe "challenge"
        query.getFirst("code_challenge_method") shouldBe "S256"
    }
})
