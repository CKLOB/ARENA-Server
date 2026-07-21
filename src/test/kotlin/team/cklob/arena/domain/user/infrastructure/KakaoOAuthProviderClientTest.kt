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

class KakaoOAuthProviderClientTest : DescribeSpec({
    it("Kakao authorization code를 token과 user API로 교환한다") {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KakaoOAuthProviderClient(
            OAuthProperties(
                kakao = OAuthProviderProperties(
                    clientId = "kakao-client",
                    clientSecret = "kakao-secret",
                    redirectUris = mapOf(ClientPlatform.WEB to "http://localhost:3000/oauth/kakao/callback"),
                ),
            ),
            builder,
        )
        server.expect(ExpectedCount.once(), requestTo("https://kauth.kakao.com/oauth/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string(containsString("client_id=kakao-client")))
            .andExpect(content().string(containsString("client_secret=kakao-secret")))
            .andExpect(content().string(containsString("redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Foauth%2Fkakao%2Fcallback")))
            .andRespond(withSuccess("""{"access_token":"kakao-access-token"}""", MediaType.APPLICATION_JSON))
        server.expect(ExpectedCount.once(), requestTo("https://kapi.kakao.com/v2/user/me"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer kakao-access-token"))
            .andRespond(withSuccess("""{"id":123,"kakao_account":{"email":"user@example.com","profile":{"profile_image_url":"https://example.com/profile.png"}}}""", MediaType.APPLICATION_JSON))

        val profile = client.authenticate("code", null, ClientPlatform.WEB)

        profile shouldBe OAuthProfile("123", "user@example.com", "https://example.com/profile.png")
        server.verify()
    }

    it("Kakao native access token으로 사용자 정보를 검증한다") {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KakaoOAuthProviderClient(OAuthProperties(), builder)
        server.expect(ExpectedCount.once(), requestTo("https://kapi.kakao.com/v2/user/me"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer native-access-token"))
            .andRespond(withSuccess("""{"id":123}""", MediaType.APPLICATION_JSON))

        client.authenticate(null, "native-access-token", ClientPlatform.ANDROID) shouldBe OAuthProfile("123", null, null)
        server.verify()
    }
})
