package team.cklob.arena.common

data class CommonApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T): CommonApiResponse<T> =
            CommonApiResponse(
                code = "SUCCESS",
                message = "성공했습니다.",
                data = data,
            )

        fun error(
            errorCode: ErrorCode,
            message: String = errorCode.message,
            data: Any? = null,
        ): CommonApiResponse<Any> =
            CommonApiResponse(
                code = errorCode.code,
                message = message,
                data = data,
            )
    }
}

data class FieldErrorDetail(
    val field: String,
    val reason: String,
)

data class ValidationErrorData(
    val fieldErrors: List<FieldErrorDetail>,
)
