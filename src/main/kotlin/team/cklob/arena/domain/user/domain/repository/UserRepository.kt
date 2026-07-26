package team.cklob.arena.domain.user.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.type.OauthProvider

interface UserRepository : JpaRepository<User, Long> {
    fun findByOauthProviderAndOauthProviderUserId(
        oauthProvider: OauthProvider,
        oauthProviderUserId: String,
    ): User?
}
