package team.cklob.arena.domain.user.application.result

import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.type.InvestmentExperience

data class UserProfileResult(
    val id: Long,
    val nickname: String,
    val email: String?,
    val profileImageUrl: String?,
    val investmentExperience: InvestmentExperience,
) {
    companion object {
        fun from(user: User): UserProfileResult =
            UserProfileResult(
                id = requireNotNull(user.id),
                nickname = user.nickname,
                email = user.email,
                profileImageUrl = user.profileImageUrl,
                investmentExperience = user.investmentExperience,
            )
    }
}
