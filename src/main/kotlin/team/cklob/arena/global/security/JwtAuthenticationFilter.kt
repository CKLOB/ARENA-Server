package team.cklob.arena.global.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val securityErrorHandler: SecurityErrorHandler,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.requestURI in REFRESH_TOKEN_PATHS

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authorization.removePrefix(BEARER_PREFIX)
        if (token.isBlank()) {
            securityErrorHandler.write(response, SecurityErrorCode.INVALID_TOKEN)
            return
        }

        try {
            val userId = jwtTokenProvider.getUserId(token)
            val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (exception: ExpiredJwtException) {
            securityErrorHandler.write(response, SecurityErrorCode.EXPIRED_TOKEN)
        } catch (exception: JwtException) {
            securityErrorHandler.write(response, SecurityErrorCode.INVALID_TOKEN)
        } catch (exception: IllegalArgumentException) {
            securityErrorHandler.write(response, SecurityErrorCode.INVALID_TOKEN)
        }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private val REFRESH_TOKEN_PATHS = setOf("/auth/refresh", "/auth/logout")
    }
}
