package team.cklob.arena.domain.trading

import org.springframework.http.HttpStatus
import team.cklob.arena.global.exception.ErrorCode

enum class TradingErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "잔액이 부족합니다."),
}
