package team.cklob.arena.domain.user.application.result

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.user.application.OAuthLoginResult

data class NewUserLoginResult(
    @field:Schema(description = "온보딩 완료 전용 JWT")
    val onboardingToken: String,
    @get:JsonProperty("isNewUser")
    override val isNewUser: Boolean = true,
) : OAuthLoginResult
