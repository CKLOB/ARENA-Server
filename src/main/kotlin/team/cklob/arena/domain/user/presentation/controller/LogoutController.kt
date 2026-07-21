package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.LogoutService
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@RestController
class LogoutController(
    private val logoutService: LogoutService,
) {
    @PostMapping("/auth/logout")
    @Operation(summary = "로그아웃")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "현재 refresh session 폐기"),
        ApiResponse(responseCode = "401", description = "유효하지 않거나 폐기된 refresh token"),
    )
    fun execute(@RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String): ResponseEntity<Void> {
        logoutService.execute(authorization.removeBearerPrefix())
        return ResponseEntity.noContent().build()
    }

    private fun String.removeBearerPrefix(): String =
        takeIf { startsWith("Bearer ") }?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
}
