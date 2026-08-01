package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.WithdrawUserService

@RestController
class WithdrawUserController(
    private val withdrawUserService: WithdrawUserService,
) {
    @DeleteMapping("/users/me")
    @Operation(summary = "회원 탈퇴", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "회원 탈퇴 완료"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
    )
    fun execute(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<Void> {
        withdrawUserService.execute(userId)
        return ResponseEntity.noContent().build()
    }
}
