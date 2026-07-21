package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.OnboardingService
import team.cklob.arena.domain.user.application.result.TokenPair
import team.cklob.arena.domain.user.presentation.request.OnboardingRequest

@RestController
class OnboardingController(
    private val onboardingService: OnboardingService,
) {
    @PostMapping("/auth/onboarding")
    @Operation(summary = "신규 사용자 온보딩")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "가입 완료 및 token pair 반환"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
        ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 onboarding token"),
    )
    fun execute(@Valid @RequestBody request: OnboardingRequest): TokenPair =
        onboardingService.execute(request.onboardingToken, request.nickname, request.investmentExperience)
}
