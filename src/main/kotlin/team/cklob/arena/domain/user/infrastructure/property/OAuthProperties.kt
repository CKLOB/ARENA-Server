package team.cklob.arena.domain.user.infrastructure.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("oauth")
data class OAuthProperties(
    val google: OAuthProviderProperties = OAuthProviderProperties(),
    val kakao: OAuthProviderProperties = OAuthProviderProperties(),
    val apple: AppleOAuthProperties = AppleOAuthProperties(),
)
