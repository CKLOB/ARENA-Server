package team.cklob.arena.challenge

import org.springframework.http.HttpStatus
import team.cklob.arena.common.ErrorCode

enum class ChallengeErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    CHALLENGE_ALREADY_ENDED(HttpStatus.CONFLICT, "이미 종료된 챌린지입니다."),
}
