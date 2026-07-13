package team.cklob.arena.security

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
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorization == null) {
            filterChain.doFilter(request, response)
            return
        }

        if (!authorization.startsWith(BEARER_PREFIX) || authorization.length == BEARER_PREFIX.length) {
            securityErrorHandler.write(response, SecurityErrorCode.INVALID_TOKEN)
            return
        }

        try {
            val userId = jwtTokenProvider.getUserId(authorization.removePrefix(BEARER_PREFIX))
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
    }
}
