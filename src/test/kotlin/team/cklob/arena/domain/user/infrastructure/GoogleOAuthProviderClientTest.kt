package team.cklob.arena.domain.user.infrastructure

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers.containsString
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties
import team.cklob.arena.domain.user.infrastructure.property.OAuthProviderProperties

class GoogleOAuthProviderClientTest : DescribeSpec({
    it("Google authorization code를 token과 userinfo API로 교환한다") {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client =
            GoogleOAuthProviderClient(
                OAuthProperties(
                    google =
                        OAuthProviderProperties(
                            clientId = "google-client",
                            clientSecret = "google-secret",
                            redirectUris = mapOf(ClientPlatform.WEB to "https://web.example.com/oauth/google"),
                        ),
                ),
                builder,
            )
        server.expect(ExpectedCount.once(), requestTo("https://oauth2.googleapis.com/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .andExpect(content().string(containsString("code=code")))
            .andExpect(content().string(containsString("client_id=google-client")))
            .andExpect(content().string(containsString("redirect_uri=https%3A%2F%2Fweb.example.com%2Foauth%2Fgoogle")))
            .andRespond(
                withSuccess(
                    """{"access_token":"google-access-token","token_type":"Bearer","expires_in":3600,"scope":"openid email"}""",
                    MediaType.APPLICATION_JSON,
                ),
            )
        server.expect(ExpectedCount.once(), requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer google-access-token"))
            .andRespond(
                withSuccess(
                    """{"sub":"google-user-id","email":"user@example.com","picture":"https://example.com/profile.png","locale":"ko"}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val profile = client.authenticate("code", null, ClientPlatform.WEB)

        profile shouldBe OAuthProfile("google-user-id", "user@example.com", "https://example.com/profile.png")
        server.verify()
    }
})
