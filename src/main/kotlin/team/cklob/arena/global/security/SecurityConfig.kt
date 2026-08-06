package team.cklob.arena.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import team.cklob.arena.domain.user.domain.repository.UserRepository

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun jwtTokenProvider(jwtProperties: JwtProperties): JwtTokenProvider = JwtTokenProvider(jwtProperties)

    @Bean
    fun securityErrorHandler(objectMapper: ObjectMapper): SecurityErrorHandler = SecurityErrorHandler(objectMapper)

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtTokenProvider: JwtTokenProvider,
        securityErrorHandler: SecurityErrorHandler,
        userRepository: UserRepository,
    ): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(securityErrorHandler)
                it.accessDeniedHandler(securityErrorHandler)
            }.authorizeHttpRequests {
                it.requestMatchers(
                    "/auth/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/actuator/health",
                    "/error",
                ).permitAll()
                it.requestMatchers("/observability/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }.addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider, securityErrorHandler, userRepository),
                UsernamePasswordAuthenticationFilter::class.java,
            ).build()
}
