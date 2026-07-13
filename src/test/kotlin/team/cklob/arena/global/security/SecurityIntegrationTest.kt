package team.cklob.arena.global.security

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.TestApi::class)
class SecurityIntegrationTest(
    private val mockMvc: MockMvc,
    private val jwtTokenProvider: JwtTokenProvider,
) : DescribeSpec({
        extension(SpringExtension)

        describe("공통 응답 및 보안 설정") {
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
        }
    }) {
    override fun extensions() = listOf(SpringExtension)

    @TestConfiguration
    @RestController
    @RequestMapping
    class TestApi {
        @GetMapping("/auth/test")
        fun publicApi() = mapOf("value" to "public")

        @GetMapping("/test/protected")
        fun protectedApi(authentication: org.springframework.security.core.Authentication) = mapOf("userId" to authentication.principal)

        @PostMapping("/auth/test")
        fun validate(
            @Valid @RequestBody request: TestRequest,
        ) = mapOf("name" to request.name)
    }

    data class TestRequest(
        @field:NotBlank
        val name: String,
    )
}
