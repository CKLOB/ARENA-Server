package team.cklob.arena.domain.market.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.market.application.GetCurrentPriceService
import team.cklob.arena.domain.market.presentation.response.CurrentPriceResponse

@RestController
class GetCurrentPriceController(
    private val getCurrentPriceService: GetCurrentPriceService,
) {
    @GetMapping("/symbols/{symbolId}/price")
    @Operation(
        summary = "현재가 조회",
        description = "Data provided by Twelve Data.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "현재가 조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "404", description = "종목 없음"),
        ApiResponse(responseCode = "409", description = "비활성 시장 또는 종목"),
        ApiResponse(responseCode = "503", description = "외부 시세 조회 실패"),
    )
    fun execute(
        @PathVariable symbolId: Long,
    ): CurrentPriceResponse = CurrentPriceResponse.from(getCurrentPriceService.execute(symbolId))
}
