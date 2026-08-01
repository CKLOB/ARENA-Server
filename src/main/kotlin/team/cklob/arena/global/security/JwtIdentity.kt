package team.cklob.arena.global.security

data class JwtIdentity(
    val userId: Long,
    val authVersion: Long,
)
