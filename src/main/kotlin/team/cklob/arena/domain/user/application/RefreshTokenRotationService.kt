package team.cklob.arena.domain.user.application

import team.cklob.arena.domain.user.application.result.TokenPair

interface RefreshTokenRotationService {
    fun execute(refreshToken: String): TokenPair
}
