package team.cklob.arena.domain.user.infrastructure

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties

class OAuthPropertiesTest : DescribeSpec({
    it("provider별 client 설정과 플랫폼별 redirect URI를 바인딩한다") {
        ApplicationContextRunner()
            .withUserConfiguration(OAuthPropertiesConfiguration::class.java)
            .withPropertyValues(
                "oauth.google.client-id=google-client",
                "oauth.google.client-secret=google-secret",
                "oauth.google.redirect-uris.web=https://web.example.com/google",
                "oauth.kakao.client-id=kakao-client",
                "oauth.kakao.client-secret=kakao-secret",
                "oauth.kakao.redirect-uris.web=http://localhost:3000/oauth/kakao/callback",
                "oauth.apple.ios-client-id=com.arena.ios",
                "oauth.apple.android-client-id=com.arena.web",
                "oauth.apple.team-id=apple-team",
                "oauth.apple.key-id=apple-key",
                "oauth.apple.private-key=test-key",
                "oauth.apple.android-redirect-uri=https://web.example.com/apple",
            ).run { context ->
                val properties = context.getBean(OAuthProperties::class.java)

                properties.google.clientId shouldBe "google-client"
                properties.google.redirectUris[ClientPlatform.WEB] shouldBe "https://web.example.com/google"
                properties.kakao.clientSecret shouldBe "kakao-secret"
                properties.kakao.redirectUris[ClientPlatform.WEB] shouldBe "http://localhost:3000/oauth/kakao/callback"
                properties.apple.privateKey shouldBe "test-key"
                properties.apple.iosClientId shouldBe "com.arena.ios"
                properties.apple.androidClientId shouldBe "com.arena.web"
                properties.apple.androidRedirectUri shouldBe "https://web.example.com/apple"
            }
    }
})

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OAuthProperties::class)
private class OAuthPropertiesConfiguration
