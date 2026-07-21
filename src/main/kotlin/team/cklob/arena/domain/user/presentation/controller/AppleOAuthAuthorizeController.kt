package team.cklob.arena.domain.user.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.AppleOAuthAuthorizationService
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.SecurityErrorCode

@RestController
class AppleOAuthAuthorizeController(
    private val appleOAuthAuthorizationService: AppleOAuthAuthorizationService,
) {
    @GetMapping("/auth/{provider}/authorize")
    @Operation(summary = "Apple Android OAuth 인가 페이지로 이동")
    @ApiResponses(
        ApiResponse(responseCode = "302", description = "Apple 인가 URL로 이동"),
        ApiResponse(responseCode = "403", description = "Apple Android 이외의 인가 요청"),
    )
    fun execute(@PathVariable provider: String, @RequestParam platform: ClientPlatform): ResponseEntity<Void> {
        if (!provider.equals(OauthProvider.APPLE.name, ignoreCase = true) || platform != ClientPlatform.ANDROID) {
            throw ExpectedException(SecurityErrorCode.FORBIDDEN)
        }
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, appleOAuthAuthorizationService.execute()).build()
    }
}
