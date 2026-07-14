package team.cklob.arena.global.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import team.cklob.arena.global.response.CommonApiResponse
import team.cklob.arena.global.response.FieldErrorDetail
import team.cklob.arena.global.response.ValidationErrorData

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ExpectedException::class)
    fun handleExpectedException(exception: ExpectedException): ResponseEntity<CommonApiResponse<Any>> =
        response(exception.errorCode, exception.message ?: exception.errorCode.message)

    @ExceptionHandler(MethodArgumentNotValidException::class, BindException::class)
    fun handleValidationException(exception: BindException): ResponseEntity<CommonApiResponse<Any>> {
        val fieldErrors =
            exception.bindingResult.fieldErrors.map {
                FieldErrorDetail(field = it.field, reason = it.defaultMessage ?: "유효하지 않은 값입니다.")
            }

        return response(
            errorCode = CommonErrorCode.INVALID_REQUEST,
            data = ValidationErrorData(fieldErrors),
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(exception: ConstraintViolationException): ResponseEntity<CommonApiResponse<Any>> =
        response(
            errorCode = CommonErrorCode.INVALID_REQUEST,
            data =
                ValidationErrorData(
                    exception.constraintViolations.map {
                        FieldErrorDetail(field = it.propertyPath.toString(), reason = it.message)
                    },
                ),
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(exception: HttpMessageNotReadableException): ResponseEntity<CommonApiResponse<Any>> =
        when (val cause = exception.cause) {
            is UnrecognizedPropertyException ->
                response(
                    errorCode = CommonErrorCode.UNKNOWN_JSON_FIELD,
                    data = ValidationErrorData(listOf(FieldErrorDetail(cause.propertyName, "알 수 없는 필드입니다."))),
                )

            is InvalidFormatException -> response(CommonErrorCode.INVALID_TYPE_VALUE)
            else -> response(CommonErrorCode.MALFORMED_JSON)
        }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException): ResponseEntity<CommonApiResponse<Any>> =
        response(CommonErrorCode.INVALID_TYPE_VALUE)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(exception: HttpRequestMethodNotSupportedException): ResponseEntity<CommonApiResponse<Any>> =
        response(CommonErrorCode.METHOD_NOT_ALLOWED)

    @ExceptionHandler(NoHandlerFoundException::class, NoResourceFoundException::class)
    fun handleNotFound(exception: Exception): ResponseEntity<CommonApiResponse<Any>> {
        return response(CommonErrorCode.RESOURCE_NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<CommonApiResponse<Any>> {
        log.error("Unhandled exception", exception)
        return response(CommonErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun response(
        errorCode: ErrorCode,
        message: String = errorCode.message,
        data: Any? = null,
    ): ResponseEntity<CommonApiResponse<Any>> =
        ResponseEntity
            .status(errorCode.status)
            .body(CommonApiResponse.error(errorCode, message, data))
}
