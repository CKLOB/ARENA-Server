package team.cklob.arena.domain.user.infrastructure

import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matchers.containsString
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import team.cklob.arena.domain.user.domain.type.ClientPlatform
import team.cklob.arena.domain.user.infrastructure.dto.OAuthProfile
import team.cklob.arena.domain.user.infrastructure.property.AppleOAuthProperties
import team.cklob.arena.domain.user.infrastructure.property.OAuthProperties
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import java.util.Date

class AppleOAuthProviderClientTest : DescribeSpec({
    it("Apple Android code를 verifier와 교환하고 서명된 id token을 검증한다") {
        val rsa = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val ec = KeyPairGenerator.getInstance("EC").apply { initialize(256) }.generateKeyPair()
        val clientId = "apple-services-id"
        val idToken =
            Jwts
                .builder()
                .header()
                .keyId("apple-jwk-id")
                .and()
                .issuer("https://appleid.apple.com")
                .subject("apple-user-id")
                .audience()
                .add(clientId)
                .and()
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .claim("email", "user@example.com")
                .signWith(rsa.private, Jwts.SIG.RS256)
                .compact()
        val stateStore = mockk<AppleOAuthStateStore>()
        every { stateStore.consume("state") } returns "verifier"
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client =
            AppleOAuthProviderClient(
                OAuthProperties(
                    apple =
                        AppleOAuthProperties(
                            androidClientId = clientId,
                            teamId = "apple-team",
                            keyId = "apple-key",
                            privateKey = privateKeyPem(ec.private.encoded).replace("\n", "\\n"),
                            androidRedirectUri = "https://web.example.com/oauth/apple",
                        ),
                ),
                stateStore,
                builder,
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper(),
            )
        server.expect(ExpectedCount.once(), requestTo("https://appleid.apple.com/auth/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string(containsString("code=code")))
            .andExpect(content().string(containsString("code_verifier=verifier")))
            .andExpect(content().string(containsString("client_secret=")))
            .andRespond(withSuccess("""{"id_token":"$idToken"}""", MediaType.APPLICATION_JSON))
        server.expect(ExpectedCount.once(), requestTo("https://appleid.apple.com/auth/keys"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(jwkSet(rsa.public as RSAPublicKey), MediaType.APPLICATION_JSON))

        val profile = client.authenticate("code", null, ClientPlatform.ANDROID, "state")

        profile shouldBe OAuthProfile("apple-user-id", "user@example.com", null)
        verify(exactly = 1) { stateStore.consume("state") }
        server.verify()
    }
}) {
    companion object {
        private fun privateKeyPem(encoded: ByteArray): String =
            "-----BEGIN PRIVATE KEY-----\n${Base64.getMimeEncoder().encodeToString(encoded)}\n-----END PRIVATE KEY-----"

        private fun jwkSet(key: RSAPublicKey): String =
            """{"keys":[{"kid":"apple-jwk-id","kty":"RSA","n":"${base64Unsigned(
                key.modulus.toByteArray(),
            )}","e":"${base64Unsigned(key.publicExponent.toByteArray())}"}]}"""

        private fun base64Unsigned(bytes: ByteArray): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.dropWhile { it == 0.toByte() }.toByteArray())
    }
}
