package team.cklob.arena.domain.learning.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.learning.application.GetDecisionLogsService
import team.cklob.arena.domain.learning.presentation.response.DecisionLogsResponse
import team.cklob.arena.domain.market.domain.type.MarketType

@RestController
class GetDecisionLogsController(
    private val getDecisionLogsService: GetDecisionLogsService,
) {
    @GetMapping("/observability/decisions")
    @Operation(summary = "결정 로그 목록 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "결정 로그 조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
    )
    fun execute(
        @RequestParam(required = false) challengeId: Long?,
        @RequestParam(required = false) market: MarketType?,
        @RequestParam(required = false) modelVersion: String?,
    ): DecisionLogsResponse = DecisionLogsResponse.from(getDecisionLogsService.execute(challengeId, market, modelVersion))
}
