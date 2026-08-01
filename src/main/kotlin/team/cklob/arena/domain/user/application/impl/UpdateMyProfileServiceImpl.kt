package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.UpdateMyProfileService
import team.cklob.arena.domain.user.application.UpdateProfileCommand
import team.cklob.arena.domain.user.application.result.UserProfileResult
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.global.exception.CommonErrorCode
import team.cklob.arena.global.exception.ExpectedException

@Service
class UpdateMyProfileServiceImpl(
    private val userRepository: UserRepository,
) : UpdateMyProfileService {
    @Transactional
    override fun execute(
        userId: Long,
        command: UpdateProfileCommand,
    ): UserProfileResult {
        val user =
            userRepository.findByIdAndDeletedAtIsNull(userId)
                ?: throw ExpectedException(CommonErrorCode.RESOURCE_NOT_FOUND)
        user.updateProfile(
            command.nickname,
            command.investmentExperience,
            command.profileImageUrlIncluded,
            command.profileImageUrl,
        )
        return UserProfileResult.from(user)
    }
}
