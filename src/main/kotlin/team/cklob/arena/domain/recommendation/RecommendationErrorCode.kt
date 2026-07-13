package team.cklob.arena.domain.recommendation

import org.springframework.http.HttpStatus
import team.cklob.arena.global.exception.ErrorCode

enum class RecommendationErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    RECOMMENDATION_ALREADY_REACTED(HttpStatus.CONFLICT, "이미 반응한 추천입니다."),
}
