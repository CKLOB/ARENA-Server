package team.cklob.arena.user

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
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

enum class InvestmentExperience {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

enum class OauthProvider {
    GOOGLE,
    KAKAO,
    APPLE,
}

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
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
}

interface UserRepository : JpaRepository<User, Long>
