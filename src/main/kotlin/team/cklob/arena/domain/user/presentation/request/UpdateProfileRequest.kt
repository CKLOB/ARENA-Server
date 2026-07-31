package team.cklob.arena.domain.user.presentation.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import io.swagger.v3.oas.annotations.media.Schema
import team.cklob.arena.domain.user.application.UpdateProfileCommand
import team.cklob.arena.domain.user.domain.type.InvestmentExperience
import team.cklob.arena.global.exception.CommonErrorCode
import team.cklob.arena.global.exception.ExpectedException

class UpdateProfileRequest {
    private var nicknameValue: String? = null
    private var investmentExperienceValue: InvestmentExperience? = null
    private var profileImageUrlValue: String? = null

    @get:JsonIgnore
    var nicknameIncluded: Boolean = false
        private set

    @get:JsonIgnore
    var investmentExperienceIncluded: Boolean = false
        private set

    @get:JsonIgnore
    var profileImageUrlIncluded: Boolean = false
        private set

    @get:Schema(description = "변경할 닉네임", example = "arena", maxLength = 50)
    val nickname: String?
        get() = nicknameValue

    @get:Schema(description = "변경할 투자 경험", example = "BEGINNER")
    val investmentExperience: InvestmentExperience?
        get() = investmentExperienceValue

    @get:Schema(description = "변경할 프로필 이미지 URL. null이면 이미지를 삭제합니다.", nullable = true, maxLength = 500)
    val profileImageUrl: String?
        get() = profileImageUrlValue

    @JsonSetter("nickname")
    fun readNickname(value: String?) {
        nicknameIncluded = true
        nicknameValue = value
    }

    @JsonSetter("investmentExperience")
    fun readInvestmentExperience(value: InvestmentExperience?) {
        investmentExperienceIncluded = true
        investmentExperienceValue = value
    }

    @JsonSetter("profileImageUrl")
    fun readProfileImageUrl(value: String?) {
        profileImageUrlIncluded = true
        profileImageUrlValue = value
    }

    fun toCommand(): UpdateProfileCommand {
        if (!nicknameIncluded && !investmentExperienceIncluded && !profileImageUrlIncluded) invalid()
        val nickname =
            if (nicknameIncluded) {
                nicknameValue?.trim()?.takeIf { it.isNotEmpty() && it.length <= 50 } ?: invalid()
            } else {
                null
            }
        val investmentExperience =
            if (investmentExperienceIncluded) {
                investmentExperienceValue ?: invalid()
            } else {
                null
            }
        val profileImageUrl =
            profileImageUrlValue?.trim()?.takeIf { it.isNotEmpty() && it.length <= 500 }
                ?: if (profileImageUrlValue == null) null else invalid()

        return UpdateProfileCommand(nickname, investmentExperience, profileImageUrlIncluded, profileImageUrl)
    }

    private fun invalid(): Nothing = throw ExpectedException(CommonErrorCode.INVALID_REQUEST)
}
