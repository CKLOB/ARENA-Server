package team.cklob.arena.domain.learning.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.learning.application.PromoteModelService
import team.cklob.arena.domain.learning.presentation.response.PromoteModelResponse

@RestController
class PromoteModelController(
    private val promoteModelService: PromoteModelService,
) {
    @PatchMapping("/observability/models/{modelVersionId}/promote")
    @Operation(summary = "모델 승격", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "모델 승격 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        ApiResponse(responseCode = "404", description = "모델 버전 없음"),
        ApiResponse(responseCode = "409", description = "승격할 수 없는 모델 상태"),
    )
    fun execute(
        @PathVariable modelVersionId: Long,
    ): PromoteModelResponse = PromoteModelResponse.from(promoteModelService.execute(modelVersionId))
}
