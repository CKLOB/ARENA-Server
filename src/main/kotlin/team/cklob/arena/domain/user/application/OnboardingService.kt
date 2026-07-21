package team.cklob.arena.domain.user.application

import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.application.result.TokenPair

interface OnboardingService {
    fun execute(onboardingToken: String, nickname: String, investmentExperience: InvestmentExperience): TokenPair
}
