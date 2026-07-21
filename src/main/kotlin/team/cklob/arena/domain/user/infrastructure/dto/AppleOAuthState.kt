package team.cklob.arena.domain.user.infrastructure.dto

data class AppleOAuthState(
    val state: String,
    val challenge: String,
)
