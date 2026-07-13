package team.cklob.arena.security

import org.springframework.http.HttpStatus
import team.cklob.arena.common.ErrorCode

enum class SecurityErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
}
