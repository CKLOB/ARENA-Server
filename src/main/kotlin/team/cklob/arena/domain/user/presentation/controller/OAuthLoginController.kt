package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.application.OAuthLoginResult
import team.cklob.arena.domain.user.application.OAuthLoginService
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.presentation.request.OAuthLoginRequest
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@RestController
class OAuthLoginController(
    private val oauthLoginService: OAuthLoginService,
) {
    @PostMapping("/auth/login/{provider}")
    @Operation(summary = "OAuth 로그인")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "기존 사용자는 token pair, 신규 사용자는 onboarding token 반환"),
        ApiResponse(responseCode = "401", description = "유효하지 않은 OAuth credential"),
        ApiResponse(responseCode = "403", description = "지원하지 않는 provider"),
    )
    fun execute(
        @PathVariable provider: String,
        @Valid @RequestBody request: OAuthLoginRequest,
    ): OAuthLoginResult =
        oauthLoginService.execute(
            provider.toOauthProvider(),
            request.authorizationCode,
            request.accessToken,
            request.platform,
            request.state,
        )

    private fun String.toOauthProvider(): OauthProvider =
        OauthProvider.entries.find { it.name.equals(this, ignoreCase = true) }
            ?: throw ExpectedException(SecurityErrorCode.FORBIDDEN)
}
