package team.cklob.arena.domain.user.infrastructure

import team.cklob.arena.domain.user.infrastructure.dto.AppleOAuthState

interface AppleOAuthStateStore {
    fun create(): AppleOAuthState

    fun consume(state: String): String
}
