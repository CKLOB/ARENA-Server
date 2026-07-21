package team.cklob.arena.domain.user.infrastructure

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile

class OAuthProviderClientResolverTest : DescribeSpec({
    it("provider에 맞는 client를 반환한다") {
        val client = object : OAuthProviderClient {
            override val provider = OauthProvider.GOOGLE

            override fun authenticate(authorizationCode: String?, accessToken: String?, platform: ClientPlatform, state: String?) =
                OAuthProfile("id", null, null)
        }

        OAuthProviderClientResolver(listOf(client)).resolve(OauthProvider.GOOGLE) shouldBe client
    }
})
