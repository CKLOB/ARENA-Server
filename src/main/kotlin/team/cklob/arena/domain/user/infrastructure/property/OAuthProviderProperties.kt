package team.cklob.arena.domain.user.infrastructure.property

import team.cklob.arena.domain.user.domain.type.ClientPlatform

data class OAuthProviderProperties(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUris: Map<ClientPlatform, String> = emptyMap(),
)
