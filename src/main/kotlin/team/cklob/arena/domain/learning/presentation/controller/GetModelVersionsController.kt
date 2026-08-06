package team.cklob.arena.domain.learning.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.learning.application.GetModelVersionsService
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import team.cklob.arena.domain.learning.presentation.response.ModelVersionsResponse

@RestController
class GetModelVersionsController(
    private val getModelVersionsService: GetModelVersionsService,
) {
    @GetMapping("/observability/models")
    @Operation(summary = "모델 버전 목록 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "모델 버전 조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
    )
    fun execute(
        @RequestParam(required = false) status: ModelStatus?,
    ): ModelVersionsResponse = ModelVersionsResponse.from(getModelVersionsService.execute(status))
}
