package team.cklob.arena.domain.user

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.global.security.RefreshTokenHasher
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
class RefreshSessionRepositoryTest(
    private val userRepository: UserRepository,
    private val refreshSessionRepository: RefreshSessionRepository,
) : DescribeSpec({
        extension(SpringExtension)

        describe("RefreshSession repository") {
            it("token hash와 만료 및 폐기 상태를 저장한다") {
                val user =
                    userRepository.save(
                        User(
                            nickname = "arena",
                            investmentExperience = InvestmentExperience.BEGINNER,
                            oauthProvider = OauthProvider.GOOGLE,
                            oauthProviderUserId = UUID.randomUUID().toString(),
                        ),
                    )
                val expiresAt = LocalDateTime.of(2026, 8, 5, 12, 0)
                val tokenHash = RefreshTokenHasher.hash("refresh-token")
                val session =
                    refreshSessionRepository.save(
                        RefreshSession(
                            user = user,
                            tokenHash = tokenHash,
                            expiresAt = expiresAt,
                        ),
                    )

                val saved = refreshSessionRepository.findById(requireNotNull(session.id)).orElseThrow()
                saved.tokenHash shouldBe tokenHash
                saved.expiresAt shouldBe expiresAt
                saved.revokedAt shouldBe null
                refreshSessionRepository.findByTokenHash(tokenHash)?.id shouldBe session.id
                RefreshSession::class.members.none { it.name == "token" } shouldBe true
            }
        }
    }) {
    override fun extensions() = listOf(SpringExtension)
}
