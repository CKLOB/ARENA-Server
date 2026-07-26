package team.cklob.arena.global.security

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import java.time.Duration

class JwtPropertiesTest : DescribeSpec({
    it("refresh token 만료 설정을 환경 속성에서 바인딩한다") {
        ApplicationContextRunner()
            .withUserConfiguration(JwtPropertiesConfiguration::class.java)
            .withPropertyValues(
                "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "security.jwt.access-token-expiration=PT15M",
                "security.jwt.refresh-token-expiration=P1D",
            ).run { context ->
                context.getBean(JwtProperties::class.java).refreshTokenExpiration shouldBe Duration.ofDays(1)
            }
    }
})

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties::class)
private class JwtPropertiesConfiguration
