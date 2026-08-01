package team.cklob.arena.domain.user.application

interface WithdrawUserService {
    fun execute(userId: Long)
}
