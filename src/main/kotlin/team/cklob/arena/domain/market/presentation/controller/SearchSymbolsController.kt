package team.cklob.arena.domain.market.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.domain.market.application.SearchSymbolsService
import team.cklob.arena.domain.market.domain.type.MarketType
import team.cklob.arena.domain.market.presentation.response.SymbolPageResponse

@Validated
@RestController
class SearchSymbolsController(
    private val searchSymbolsService: SearchSymbolsService,
) {
    @GetMapping("/symbols/search")
    @Operation(
        summary = "종목 검색",
        description = "종목 코드 또는 이름을 대소문자 구분 없이 부분 검색합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "종목 검색 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "409", description = "비활성 시장"),
    )
    fun execute(
        @RequestParam market: MarketType,
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) size: Int,
    ): SymbolPageResponse = SymbolPageResponse.from(searchSymbolsService.execute(market, keyword, page, size))
}
