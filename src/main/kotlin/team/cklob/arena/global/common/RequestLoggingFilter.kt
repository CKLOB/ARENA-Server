package team.cklob.arena.global.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI.removePrefix(request.contextPath)
        return EXCLUDED_PATH_PREFIXES.any { path.startsWith(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = UUID.randomUUID().toString()
        val startedAt = System.nanoTime()

        MDC.put(REQUEST_ID_KEY, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsedMillis = (System.nanoTime() - startedAt) / NANOS_PER_MILLISECOND
            log.info(
                "requestId={} method={} path={} status={} elapsedMs={}",
                requestId,
                request.method,
                request.requestURI,
                response.status,
                elapsedMillis,
            )
            MDC.remove(REQUEST_ID_KEY)
        }
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"

        private const val REQUEST_ID_KEY = "requestId"
        private const val NANOS_PER_MILLISECOND = 1_000_000
        private val EXCLUDED_PATH_PREFIXES = listOf("/swagger-ui", "/v3/api-docs", "/actuator")
    }
}
