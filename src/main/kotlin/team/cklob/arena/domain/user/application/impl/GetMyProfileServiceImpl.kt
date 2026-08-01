package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.GetMyProfileService
import team.cklob.arena.domain.user.application.result.UserProfileResult
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.global.exception.CommonErrorCode
import team.cklob.arena.global.exception.ExpectedException

@Service
class GetMyProfileServiceImpl(
    private val userRepository: UserRepository,
) : GetMyProfileService {
    @Transactional(readOnly = true)
    override fun execute(userId: Long): UserProfileResult =
        userRepository.findByIdAndDeletedAtIsNull(userId)
            ?.let(UserProfileResult::from)
            ?: throw ExpectedException(CommonErrorCode.RESOURCE_NOT_FOUND)
}
