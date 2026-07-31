package team.cklob.arena.domain.user.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.cklob.arena.domain.user.application.WithdrawUserService
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.domain.user.infrastructure.RefreshTokenStore
import team.cklob.arena.global.exception.CommonErrorCode
import team.cklob.arena.global.exception.ExpectedException
import java.time.LocalDateTime

@Service
class WithdrawUserServiceImpl(
    private val userRepository: UserRepository,
    private val refreshSessionRepository: RefreshSessionRepository,
    private val refreshTokenStore: RefreshTokenStore,
) : WithdrawUserService {
    @Transactional
    override fun execute(userId: Long) {
        val user =
            userRepository.findByIdForUpdate(userId)
                ?.takeIf { it.deletedAt == null }
                ?: throw ExpectedException(CommonErrorCode.RESOURCE_NOT_FOUND)
        val now = LocalDateTime.now()
        user.withdraw(now)
        refreshSessionRepository.findAllByUserIdAndRevokedAtIsNull(userId).forEach {
            it.revoke(now)
            refreshTokenStore.delete(requireNotNull(it.id))
        }
    }
}
