package team.cklob.arena.domain.user.application.result

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.user.application.OAuthLoginResult

data class ExistingUserLoginResult(
    @field:Schema(description = "보호 API용 access JWT")
    val accessToken: String,
    @field:Schema(description = "기기별 refresh JWT")
    val refreshToken: String,
    @get:JsonProperty("isNewUser")
    override val isNewUser: Boolean = false,
) : OAuthLoginResult
