package team.cklob.arena.domain.user.application

import team.cklob.arena.domain.user.application.result.UserProfileResult

interface UpdateMyProfileService {
    fun execute(
        userId: Long,
        command: UpdateProfileCommand,
    ): UserProfileResult
}
