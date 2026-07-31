package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.GetMyProfileService
import team.cklob.arena.domain.user.presentation.response.UserProfileResponse

@RestController
class GetMyProfileController(
    private val getMyProfileService: GetMyProfileService,
) {
    @GetMapping("/users/me")
    @Operation(summary = "내 프로필 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
    )
    fun execute(
        @AuthenticationPrincipal userId: Long,
    ): UserProfileResponse = UserProfileResponse.from(getMyProfileService.execute(userId))
}
