package team.cklob.arena.domain.user.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.user.application.result.UserProfileResult
import team.cklob.arena.domain.user.domain.type.InvestmentExperience

data class UserProfileResponse(
    @field:Schema(example = "1")
    val id: Long,
    @field:Schema(example = "arena")
    val nickname: String,
    @field:Schema(example = "arena@example.com", nullable = true)
    val email: String?,
    @field:Schema(example = "https://example.com/profile.png", nullable = true)
    val profileImageUrl: String?,
    @field:Schema(example = "BEGINNER")
    val investmentExperience: InvestmentExperience,
) {
    companion object {
        fun from(result: UserProfileResult): UserProfileResponse =
            UserProfileResponse(
                id = result.id,
                nickname = result.nickname,
                email = result.email,
                profileImageUrl = result.profileImageUrl,
                investmentExperience = result.investmentExperience,
            )
    }
}
