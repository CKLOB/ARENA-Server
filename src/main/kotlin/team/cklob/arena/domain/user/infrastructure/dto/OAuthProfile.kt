package team.cklob.arena.domain.user.infrastructure.dto

data class OAuthProfile(
    val providerUserId: String,
    val email: String?,
    val profileImageUrl: String?,
)
