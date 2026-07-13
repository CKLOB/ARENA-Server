package team.cklob.arena.recommendation

import org.springframework.http.HttpStatus
import team.cklob.arena.common.ErrorCode

enum class RecommendationErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    RECOMMENDATION_ALREADY_REACTED(HttpStatus.CONFLICT, "이미 반응한 추천입니다."),
}
