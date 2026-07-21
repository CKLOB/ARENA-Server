package team.cklob.arena.domain.user.application

interface LogoutService {
    fun execute(refreshToken: String)
}
