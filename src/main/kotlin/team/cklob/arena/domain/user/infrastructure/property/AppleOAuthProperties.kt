package team.cklob.arena.domain.user.infrastructure.property

data class AppleOAuthProperties(
    val iosClientId: String = "",
    val androidClientId: String = "",
    val teamId: String = "",
    val keyId: String = "",
    val privateKey: String = "",
    val androidRedirectUri: String = "",
)
