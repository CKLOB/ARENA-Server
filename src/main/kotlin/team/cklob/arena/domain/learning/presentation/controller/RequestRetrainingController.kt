package team.cklob.arena.domain.learning.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.learning.application.RequestRetrainingService
import team.cklob.arena.domain.learning.presentation.response.RetrainingResponse

@RestController
class RequestRetrainingController(
    private val requestRetrainingService: RequestRetrainingService,
) {
    @PostMapping("/observability/models/retrain")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "모델 재학습 수동 트리거", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "202", description = "재학습 요청 접수"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        ApiResponse(responseCode = "500", description = "외부 서비스 호출 실패"),
    )
    fun execute(): RetrainingResponse = RetrainingResponse.from(requestRetrainingService.execute())
}
