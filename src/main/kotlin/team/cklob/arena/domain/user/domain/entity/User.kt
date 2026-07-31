package team.cklob.arena.domain.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.domain.type.OauthProvider
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_users_oauth", columnNames = ["oauth_provider", "oauth_provider_user_id"]),
    ],
)
class User(
    @Column(name = "email", length = 255)
    var email: String? = null,
    @Column(name = "nickname", nullable = false, length = 50)
    var nickname: String,
    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience", nullable = false, length = 20)
    var investmentExperience: InvestmentExperience,
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    var oauthProvider: OauthProvider,
    @Column(name = "oauth_provider_user_id", nullable = false, length = 255)
    var oauthProviderUserId: String,
    @Column(name = "oauth_access_token", columnDefinition = "TEXT")
    var oauthAccessToken: String? = null,
    @Column(name = "oauth_refresh_token", columnDefinition = "TEXT")
    var oauthRefreshToken: String? = null,
    @Column(name = "auth_version", nullable = false)
    var authVersion: Long = 0,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    fun updateProfile(
        nickname: String?,
        investmentExperience: InvestmentExperience?,
        profileImageUrlIncluded: Boolean,
        profileImageUrl: String?,
    ) {
        nickname?.let { this.nickname = it }
        investmentExperience?.let { this.investmentExperience = it }
        if (profileImageUrlIncluded) this.profileImageUrl = profileImageUrl
    }

    fun withdraw(now: LocalDateTime) {
        deletedAt = now
        authVersion++
    }

    fun restore() {
        deletedAt = null
    }
}
