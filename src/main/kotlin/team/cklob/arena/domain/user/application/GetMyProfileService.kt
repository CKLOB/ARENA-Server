package team.cklob.arena.domain.user.application

import team.cklob.arena.domain.user.application.result.UserProfileResult

interface GetMyProfileService {
    fun execute(userId: Long): UserProfileResult
}
