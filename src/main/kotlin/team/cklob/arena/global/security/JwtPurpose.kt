package team.cklob.arena.global.security

enum class JwtPurpose(
    val claimValue: String,
) {
    ACCESS("access"),
    REFRESH("refresh"),
    ONBOARDING("onboarding"),
}
