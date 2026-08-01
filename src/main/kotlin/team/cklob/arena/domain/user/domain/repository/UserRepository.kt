package team.cklob.arena.domain.user.domain.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.type.OauthProvider

interface UserRepository : JpaRepository<User, Long> {
    fun findByOauthProviderAndOauthProviderUserId(
        oauthProvider: OauthProvider,
        oauthProviderUserId: String,
    ): User?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select user from User user
        where user.oauthProvider = :oauthProvider
          and user.oauthProviderUserId = :oauthProviderUserId
        """,
    )
    fun findByOauthProviderAndOauthProviderUserIdForUpdate(
        @Param("oauthProvider") oauthProvider: OauthProvider,
        @Param("oauthProviderUserId") oauthProviderUserId: String,
    ): User?

    fun findByIdAndDeletedAtIsNull(id: Long): User?

    fun existsByIdAndDeletedAtIsNullAndAuthVersion(
        id: Long,
        authVersion: Long,
    ): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): User?
}
