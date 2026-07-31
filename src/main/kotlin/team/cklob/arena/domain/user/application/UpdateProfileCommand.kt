package team.cklob.arena.domain.user.application

import team.cklob.arena.domain.user.domain.type.InvestmentExperience

data class UpdateProfileCommand(
    val nickname: String?,
    val investmentExperience: InvestmentExperience?,
    val profileImageUrlIncluded: Boolean,
    val profileImageUrl: String?,
)
