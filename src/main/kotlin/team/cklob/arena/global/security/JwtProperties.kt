package team.cklob.arena.global.security

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Validated
@ConfigurationProperties("security.jwt")
data class JwtProperties(
    /** Base64-encoded secret with at least 32 bytes before encoding. */
    @field:NotBlank
    val secret: String,
    val accessTokenExpiration: Duration,
)
