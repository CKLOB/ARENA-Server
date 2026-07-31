package team.cklob.arena.global.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    fun createAccessToken(
        userId: Long,
        authVersion: Long = 0,
    ): String =
        createToken(
            userId.toString(),
            JwtPurpose.ACCESS,
            jwtProperties.accessTokenExpiration,
            mapOf(AUTH_VERSION_CLAIM to authVersion),
        )

    fun createRefreshToken(
        userId: Long,
        sessionId: Long,
        authVersion: Long = 0,
    ): String =
        createToken(
            userId.toString(),
            JwtPurpose.REFRESH,
            jwtProperties.refreshTokenExpiration,
            mapOf(
                REFRESH_SESSION_ID_CLAIM to sessionId,
                AUTH_VERSION_CLAIM to authVersion,
            ),
        )

    fun createOnboardingToken(subject: String): String =
        createToken(
            subject,
            JwtPurpose.ONBOARDING,
            jwtProperties.onboardingTokenExpiration,
        )

    fun getUserId(token: String): Long = getUserId(token, JwtPurpose.ACCESS)

    fun getUserId(
        token: String,
        expectedPurpose: JwtPurpose,
    ): Long = getSubject(token, expectedPurpose).toLong()

    fun getSubject(
        token: String,
        expectedPurpose: JwtPurpose,
    ): String = getClaims(token, expectedPurpose).subject

    fun getRefreshSessionId(token: String): Long = (getClaims(token, JwtPurpose.REFRESH)[REFRESH_SESSION_ID_CLAIM] as Number).toLong()

    fun getIdentity(
        token: String,
        expectedPurpose: JwtPurpose,
    ): JwtIdentity {
        val claims = getClaims(token, expectedPurpose)
        return JwtIdentity(
            userId = claims.subject.toLong(),
            authVersion = (claims[AUTH_VERSION_CLAIM] as? Number)?.toLong() ?: 0,
        )
    }

    private fun getClaims(
        token: String,
        expectedPurpose: JwtPurpose,
    ): Claims {
        val claims = parseClaims(token)
        if (claims[JWT_PURPOSE_CLAIM] != expectedPurpose.claimValue) throw UnsupportedJwtException("Unexpected JWT purpose")
        return claims
    }

    private fun createToken(
        subject: String,
        purpose: JwtPurpose,
        expiration: java.time.Duration,
        claims: Map<String, Any> = emptyMap(),
    ): String {
        val now = Instant.now()
        return Jwts.builder().subject(subject).claim(JWT_PURPOSE_CLAIM, purpose.claimValue).claims(claims)
            .issuedAt(Date.from(now)).expiration(Date.from(now.plus(expiration))).signWith(signingKey()).compact()
    }

    private fun parseClaims(token: String): Claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).payload

    private fun signingKey(): SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    private companion object {
        const val JWT_PURPOSE_CLAIM = "purpose"
        const val REFRESH_SESSION_ID_CLAIM = "sid"
        const val AUTH_VERSION_CLAIM = "ver"
    }
}
