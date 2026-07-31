package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.UpdateMyProfileService
import team.cklob.arena.domain.user.presentation.request.UpdateProfileRequest
import team.cklob.arena.domain.user.presentation.response.UserProfileResponse

@RestController
class UpdateMyProfileController(
    private val updateMyProfileService: UpdateMyProfileService,
) {
    @PatchMapping("/users/me")
    @Operation(summary = "내 프로필 수정", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 수정 값"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
    )
    fun execute(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: UpdateProfileRequest,
    ): UserProfileResponse = UserProfileResponse.from(updateMyProfileService.execute(userId, request.toCommand()))
}
