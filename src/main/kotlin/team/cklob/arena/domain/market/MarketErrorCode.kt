package team.cklob.arena.domain.market

import org.springframework.http.HttpStatus
import team.cklob.arena.global.exception.ErrorCode

enum class MarketErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    MARKET_NOT_ACTIVE(HttpStatus.CONFLICT, "현재 이용할 수 없는 시장입니다."),
    SYMBOL_NOT_FOUND(HttpStatus.NOT_FOUND, "종목을 찾을 수 없습니다."),
    SYMBOL_NOT_ACTIVE(HttpStatus.CONFLICT, "현재 이용할 수 없는 종목입니다."),
    MARKET_DATA_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "시세 정보를 일시적으로 조회할 수 없습니다."),
    SYMBOL_SYNC_REJECTED(HttpStatus.SERVICE_UNAVAILABLE, "종목 동기화 응답이 불완전하여 기존 데이터를 유지합니다."),
    INVALID_PRICE_HISTORY_RANGE(HttpStatus.BAD_REQUEST, "가격 조회 기간은 최대 7일이며 시작일이 종료일보다 늦을 수 없습니다."),
}
