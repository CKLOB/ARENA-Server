package team.cklob.arena.domain.learning.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.learning.application.GetShapExplanationService
import team.cklob.arena.domain.learning.presentation.response.ShapExplanationResponse

@RestController
class GetShapExplanationController(
    private val getShapExplanationService: GetShapExplanationService,
) {
    @GetMapping("/observability/decisions/{decisionId}/shap")
    @Operation(summary = "결정 SHAP 설명 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "SHAP 설명 조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        ApiResponse(responseCode = "404", description = "결정 로그 없음"),
        ApiResponse(responseCode = "500", description = "외부 서비스 호출 실패"),
    )
    fun execute(
        @PathVariable decisionId: Long,
    ): ShapExplanationResponse = ShapExplanationResponse.from(getShapExplanationService.execute(decisionId))
}
