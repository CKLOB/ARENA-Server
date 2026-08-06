package team.cklob.arena.domain.learning

import org.springframework.http.HttpStatus
import team.cklob.arena.global.exception.ErrorCode

enum class LearningErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    MODEL_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "모델 버전을 찾을 수 없습니다."),
    MODEL_STATUS_CONFLICT(HttpStatus.CONFLICT, "Challenger 상태의 모델만 승격할 수 있습니다."),
    DECISION_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "결정 로그를 찾을 수 없습니다."),
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "외부 AI 서비스를 호출할 수 없습니다."),
}
