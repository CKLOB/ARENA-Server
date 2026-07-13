package team.cklob.arena.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    fun createAccessToken(userId: Long): String {
        val now = Instant.now()
        return Jwts
            .builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(jwtProperties.accessTokenExpiration)))
            .signWith(signingKey())
            .compact()
    }

    fun getUserId(token: String): Long = parseClaims(token).subject.toLong()

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .payload

    private fun signingKey(): SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
}
