package team.cklob.arena.global.security

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.cklob.arena.global.common.RequestLoggingFilter
import team.cklob.arena.domain.user.domain.entity.RefreshSession
import team.cklob.arena.domain.user.domain.entity.User
import team.cklob.arena.domain.user.domain.repository.RefreshSessionRepository
import team.cklob.arena.domain.user.domain.repository.UserRepository
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.domain.user.domain.type.OauthProvider
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.OAuthProviderClient
import team.cklob.arena.global.exception.ExpectedException
import team.cklob.arena.global.security.RefreshTokenHasher
import io.mockk.every
import io.mockk.mockk
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(
    properties = [
        "oauth.google.enabled=false",
        "oauth.apple.android-client-id=apple-services-id",
        "oauth.apple.android-redirect-uri=https://web.example.com/oauth/apple",
    ],
)
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.TestApi::class)
class SecurityIntegrationTest(
    private val mockMvc: MockMvc,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val refreshSessionRepository: RefreshSessionRepository,
) : DescribeSpec({
        extension(SpringExtension)

        describe("공통 응답 및 보안 설정") {
            it("서버 생성 request ID를 응답 헤더에 반환한다") {
                val result =
                    mockMvc
                        .get("/auth/test") {
                            header(RequestLoggingFilter.REQUEST_ID_HEADER, "client-request-id")
                            accept = MediaType.APPLICATION_JSON
                        }.andExpect {
                            status { isOk() }
                            header { exists(RequestLoggingFilter.REQUEST_ID_HEADER) }
                        }.andReturn()

                val requestId = result.response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER)
                (requestId == "client-request-id") shouldBe false
                runCatching { UUID.fromString(requireNotNull(requestId)) }.isSuccess shouldBe true
            }

            it("Bearer가 아닌 Authorization 헤더는 공개 API를 차단하지 않는다") {
                mockMvc.get("/auth/test") {
                    header("Authorization", "Basic ignored")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.code") { value("SUCCESS") }
                }
            }

            it("공개 API 응답을 공통 형식으로 감싼다") {
                mockMvc.get("/auth/test") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.code") { value("SUCCESS") }
                    jsonPath("$.data.value") { value("public") }
                }
            }

            it("인증 없이 보호 API를 호출하면 공통 401 응답을 반환한다") {
                mockMvc.get("/test/protected") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.code") { value("UNAUTHORIZED") }
                }
            }

            it("유효한 Bearer JWT로 보호 API를 호출할 수 있다") {
                mockMvc.get("/test/protected") {
                    header("Authorization", "Bearer ${jwtTokenProvider.createAccessToken(1L)}")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.code") { value("SUCCESS") }
                    jsonPath("$.data.userId") { value(1) }
                }
            }

            it("refresh JWT로 보호 API를 호출하면 공통 401 응답을 반환한다") {
                mockMvc.get("/test/protected") {
                    header("Authorization", "Bearer ${jwtTokenProvider.createRefreshToken(1L, 1L)}")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.code") { value("INVALID_TOKEN") }
                }
            }

            it("onboarding JWT로 보호 API를 호출하면 공통 401 응답을 반환한다") {
                mockMvc.get("/test/protected") {
                    header("Authorization", "Bearer ${jwtTokenProvider.createOnboardingToken("GOOGLE:new-user")}")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.code") { value("INVALID_TOKEN") }
                }
            }

            it("저장되지 않은 refresh JWT로 토큰 재발급을 요청하면 공통 401 응답을 반환한다") {
                mockMvc.post("/auth/refresh") {
                    header("Authorization", "Bearer ${jwtTokenProvider.createRefreshToken(1L, 1L)}")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.code") { value("INVALID_TOKEN") }
                }
            }

            it("유효하지 않은 OAuth authorization code는 401 응답을 반환한다") {
                mockMvc.post("/auth/login/GOOGLE") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"authorizationCode":"invalid","platform":"WEB"}"""
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.code") { value("INVALID_TOKEN") }
                }
            }

            it("지원하지 않는 OAuth provider는 403 응답을 반환한다") {
                mockMvc.post("/auth/login/UNKNOWN") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"authorizationCode":"code","platform":"WEB"}"""
                }.andExpect {
                    status { isForbidden() }
                    jsonPath("$.code") { value("FORBIDDEN") }
                }
            }

            it("기존 OAuth 사용자는 access와 refresh token을 받는다") {
                userRepository.save(User(nickname = "arena", investmentExperience = InvestmentExperience.BEGINNER, oauthProvider = OauthProvider.GOOGLE, oauthProviderUserId = "existing"))

                mockMvc.post("/auth/login/GOOGLE") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"authorizationCode":"existing","platform":"WEB"}"""
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.isNewUser") { value(false) }
                    jsonPath("$.data.accessToken") { exists() }
                    jsonPath("$.data.refreshToken") { exists() }
                    jsonPath("$.data.onboardingToken") { doesNotExist() }
                }
            }

            it("신규 OAuth 사용자는 onboarding token만 받는다") {
                mockMvc.post("/auth/login/GOOGLE") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"authorizationCode":"new","platform":"WEB"}"""
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.isNewUser") { value(true) }
                    jsonPath("$.data.onboardingToken") { exists() }
                    jsonPath("$.data.accessToken") { doesNotExist() }
                    jsonPath("$.data.refreshToken") { doesNotExist() }
                }
            }

            it("온보딩을 완료하면 access와 refresh token을 반환하고 사용자를 저장한다") {
                val providerUserId = UUID.randomUUID().toString()

                mockMvc.post("/auth/onboarding") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """{"onboardingToken":"${jwtTokenProvider.createOnboardingToken("KAKAO:$providerUserId")}","nickname":"arena","investmentExperience":"BEGINNER"}"""
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.accessToken") { exists() }
                    jsonPath("$.data.refreshToken") { exists() }
                }

                userRepository.findByOauthProviderAndOauthProviderUserId(OauthProvider.KAKAO, providerUserId)?.nickname shouldBe "arena"
            }

            it("온보딩 필수 입력이 없으면 400 응답을 반환한다") {
                mockMvc.post("/auth/onboarding") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"nickname":"","investmentExperience":"BEGINNER"}"""
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("INVALID_REQUEST") }
                }
            }

            it("Apple Android authorize 요청은 PKCE authorization URL로 redirect한다") {
                val response =
                    mockMvc.get("/auth/APPLE/authorize") {
                        param("platform", "ANDROID")
                    }.andExpect {
                        status { isFound() }
                    }.andReturn().response

                val location = requireNotNull(response.getHeader(HttpHeaders.LOCATION))
                location.startsWith("https://appleid.apple.com/auth/authorize?") shouldBe true
                location.contains("code_challenge_method=S256") shouldBe true
                location.contains("state=") shouldBe true
            }

            it("Apple Android 이외 authorize 요청은 403 응답을 반환한다") {
                mockMvc.get("/auth/GOOGLE/authorize") {
                    param("platform", "ANDROID")
                }.andExpect {
                    status { isForbidden() }
                    jsonPath("$.code") { value("FORBIDDEN") }
                }
            }

            it("저장된 refresh JWT로 토큰을 재발급하면 이전 session을 폐기한다") {
                val user = userRepository.save(User(nickname = "arena", investmentExperience = InvestmentExperience.BEGINNER, oauthProvider = OauthProvider.GOOGLE, oauthProviderUserId = UUID.randomUUID().toString()))
                val session = refreshSessionRepository.save(RefreshSession(user, "pending", LocalDateTime.now().plusDays(1)))
                val refreshToken = jwtTokenProvider.createRefreshToken(requireNotNull(user.id), requireNotNull(session.id))
                session.tokenHash = RefreshTokenHasher.hash(refreshToken)
                refreshSessionRepository.save(session)

                mockMvc.post("/auth/refresh") {
                    header("Authorization", "Bearer $refreshToken")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.accessToken") { exists() }
                    jsonPath("$.data.refreshToken") { exists() }
                }

                (refreshSessionRepository.findById(requireNotNull(session.id)).orElseThrow().revokedAt != null) shouldBe true
            }

            it("로그아웃은 현재 refresh session만 폐기하고 204를 반환한다") {
                val user = userRepository.save(User(nickname = "arena", investmentExperience = InvestmentExperience.BEGINNER, oauthProvider = OauthProvider.GOOGLE, oauthProviderUserId = UUID.randomUUID().toString()))
                val current = refreshSessionRepository.save(RefreshSession(user, "pending-current", LocalDateTime.now().plusDays(1)))
                val other = refreshSessionRepository.save(RefreshSession(user, "b".repeat(64), LocalDateTime.now().plusDays(1)))
                val refreshToken = jwtTokenProvider.createRefreshToken(requireNotNull(user.id), requireNotNull(current.id))
                current.tokenHash = RefreshTokenHasher.hash(refreshToken)
                refreshSessionRepository.save(current)

                mockMvc.post("/auth/logout") {
                    header("Authorization", "Bearer $refreshToken")
                }.andExpect {
                    status { isNoContent() }
                }

                (refreshSessionRepository.findById(requireNotNull(current.id)).orElseThrow().revokedAt != null) shouldBe true
                refreshSessionRepository.findById(requireNotNull(other.id)).orElseThrow().revokedAt shouldBe null
            }

            it("변조된 JWT는 공통 401 응답을 반환한다") {
                mockMvc.get("/test/protected") {
                    header("Authorization", "Bearer invalid-token")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.code") { value("INVALID_TOKEN") }
                }
            }

            it("검증 오류를 필드 오류 목록으로 반환한다") {
                mockMvc.post("/auth/test") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":""}"""
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("INVALID_REQUEST") }
                    jsonPath("$.data.fieldErrors[0].field") { value("name") }
                }
            }

            it("Kotlin 필수 필드 누락을 필드 오류로 반환한다") {
                mockMvc.post("/auth/test") {
                    contentType = MediaType.APPLICATION_JSON
                    content = "{}"
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("INVALID_REQUEST") }
                    jsonPath("$.data.fieldErrors[0].field") { value("name") }
                }
            }

            it("알 수 없는 JSON 필드를 요청 오류로 반환한다") {
                mockMvc.post("/auth/test") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"name":"arena","unknown":true}"""
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("UNKNOWN_JSON_FIELD") }
                    jsonPath("$.data.fieldErrors[0].field") { value("unknown") }
                }
            }

            it("시간 값을 ISO-8601 문자열로 직렬화한다") {
                mockMvc.get("/auth/time") {
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.createdAt") { value("2026-01-02T03:04:05") }
                }
            }

            it("OpenAPI 문서는 인증 없이 접근할 수 있다") {
                mockMvc.get("/v3/api-docs").andExpect {
                    status { isOk() }
                    jsonPath("$.paths['/auth/login/{provider}'].post") { exists() }
                    jsonPath("$.paths['/auth/login/{provider}'].post.responses['200']") { exists() }
                    jsonPath("$.paths['/auth/login/{provider}'].post.responses['401']") { exists() }
                    jsonPath("$.paths['/auth/{provider}/authorize'].get.responses['302']") { exists() }
                    jsonPath("$.paths['/auth/onboarding'].post") { exists() }
                    jsonPath("$.paths['/auth/onboarding'].post.responses['400']") { exists() }
                    jsonPath("$.paths['/auth/refresh'].post") { exists() }
                    jsonPath("$.paths['/auth/refresh'].post.responses['401']") { exists() }
                    jsonPath("$.paths['/auth/logout'].post") { exists() }
                    jsonPath("$.paths['/auth/logout'].post.responses['204']") { exists() }
                    jsonPath("$.components.schemas.OAuthLoginRequest.properties.authorizationCode") { exists() }
                    jsonPath("$.components.schemas.OAuthLoginRequest.properties.accessToken") { exists() }
                    jsonPath("$.components.schemas.OnboardingRequest.properties.nickname") { exists() }
                }
            }

            it("컨텍스트 경로가 있는 OpenAPI 요청은 로깅에서 제외한다") {
                val result =
                    mockMvc
                        .get("/api/v3/api-docs") {
                            contextPath = "/api"
                        }.andExpect {
                            status { isOk() }
                        }.andReturn()

                result.response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER) shouldBe null
            }

            it("Resource 응답은 공통 응답으로 감싸지 않는다") {
                mockMvc.get("/auth/resource").andExpect {
                    status { isOk() }
                    content { string("resource") }
                }
            }
        }
    }) {
    override fun extensions() = listOf(SpringExtension)

    @TestConfiguration
    @RestController
    @RequestMapping
    class TestApi {
        @Bean
        fun stringRedisTemplate(): StringRedisTemplate {
            val values = mockk<ValueOperations<String, String>>()
            every { values.set(any(), any(), any<Duration>()) } returns Unit
            every { values.get(any()) } returns null
            return mockk(relaxed = true) {
                every { opsForValue() } returns values
            }
        }

        @Bean
        fun fakeGoogleOAuthProviderClient(): OAuthProviderClient =
            object : OAuthProviderClient {
                override val provider = OauthProvider.GOOGLE

                override fun authenticate(
                    authorizationCode: String?,
                    accessToken: String?,
                    platform: ClientPlatform,
                    state: String?,
                ): OAuthProfile {
                    if (authorizationCode == "invalid") throw ExpectedException(SecurityErrorCode.INVALID_TOKEN)
                    return OAuthProfile(requireNotNull(authorizationCode), null, null)
                }
            }

        @GetMapping("/auth/test")
        fun publicApi() = mapOf("value" to "public")

        @GetMapping("/test/protected")
        fun protectedApi(authentication: org.springframework.security.core.Authentication) = mapOf("userId" to authentication.principal)

        @PostMapping("/auth/test")
        fun validate(
            @Valid @RequestBody request: TestRequest,
        ) = mapOf("name" to request.name)

        @GetMapping("/auth/time")
        fun time() = mapOf("createdAt" to LocalDateTime.of(2026, 1, 2, 3, 4, 5))

        @GetMapping("/auth/resource")
        fun resource(): Resource = ByteArrayResource("resource".toByteArray())
    }

    data class TestRequest(
        @field:NotBlank
        val name: String,
    )
}
