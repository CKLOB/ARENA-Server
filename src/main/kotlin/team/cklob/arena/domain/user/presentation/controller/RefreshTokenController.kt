package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.RefreshTokenRotationService
import team.cklob.arena.domain.user.application.result.TokenPair
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@RestController
class RefreshTokenController(
    private val refreshTokenRotationService: RefreshTokenRotationService,
) {
    @PostMapping("/auth/refresh")
    @Operation(summary = "토큰 재발급")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "새 access/refresh token 반환"),
        ApiResponse(responseCode = "401", description = "유효하지 않거나 폐기된 refresh token"),
    )
    fun execute(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): TokenPair = refreshTokenRotationService.execute(authorization.removeBearerPrefix())

    private fun String.removeBearerPrefix(): String =
        takeIf { startsWith("Bearer ") }?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }
            ?: throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
}
