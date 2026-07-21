package team.cklob.arena.domain.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import team.cklob.arena.domain.user.domain.type.InvestmentExperience

data class OnboardingRequest(
    @field:Schema(description = "신규 OAuth identity에 발급된 onboarding JWT", example = "onboarding-token")
    @field:NotBlank
    val onboardingToken: String,
    @field:Schema(description = "서비스에서 사용할 닉네임", example = "arena")
    @field:NotBlank
    val nickname: String,
    @field:Schema(description = "투자 경험", example = "BEGINNER")
    val investmentExperience: InvestmentExperience,
)
