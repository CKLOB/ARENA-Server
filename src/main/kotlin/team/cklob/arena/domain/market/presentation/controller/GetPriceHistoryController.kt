package team.cklob.arena.domain.market.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.market.application.GetPriceHistoryService
import team.cklob.arena.domain.market.presentation.response.PriceHistoryResponse
import java.time.LocalDate

@RestController
class GetPriceHistoryController(
    private val getPriceHistoryService: GetPriceHistoryService,
) {
    @GetMapping("/symbols/{symbolId}/price-history")
    @Operation(
        summary = "가격 히스토리 조회",
        description = "UTC 기준 1시간봉을 최대 7일 조회합니다. Data provided by Twelve Data.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "가격 히스토리 조회 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 날짜 범위"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "종목 없음"),
        ApiResponse(responseCode = "409", description = "비활성 시장 또는 종목"),
        ApiResponse(responseCode = "503", description = "외부 시세 조회 실패"),
    )
    fun execute(
        @PathVariable symbolId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): PriceHistoryResponse = PriceHistoryResponse.from(getPriceHistoryService.execute(symbolId, from, to))
}
