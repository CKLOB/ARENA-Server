package team.cklob.arena.common

import org.springframework.http.HttpStatus

data class CommonApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T): CommonApiResponse<T> =
            CommonApiResponse(
                code = ErrorCode.SUCCESS.name,
                message = ErrorCode.SUCCESS.message,
                data = data,
            )

        fun error(
            errorCode: ErrorCode,
            message: String = errorCode.message,
            data: Any? = null,
        ): CommonApiResponse<Any> =
            CommonApiResponse(
                code = errorCode.name,
                message = message,
                data = data,
            )
    }
}

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    SUCCESS(HttpStatus.OK, "성공했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "잔액이 부족합니다."),
    CHALLENGE_ALREADY_ENDED(HttpStatus.CONFLICT, "이미 종료된 챌린지입니다."),
    RECOMMENDATION_ALREADY_REACTED(HttpStatus.CONFLICT, "이미 반응한 추천입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
}

data class FieldErrorDetail(
    val field: String,
    val reason: String,
)

data class ValidationErrorData(
    val fieldErrors: List<FieldErrorDetail>,
)
