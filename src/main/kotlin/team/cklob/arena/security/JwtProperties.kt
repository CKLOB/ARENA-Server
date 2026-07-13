package team.cklob.arena.security

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Validated
@ConfigurationProperties("security.jwt")
data class JwtProperties(
    @field:NotBlank
    val secret: String,
    val accessTokenExpiration: Duration,
)
