package team.cklob.arena.domain.learning.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.cklob.arena.domain.learning.LearningErrorCode
import team.cklob.arena.domain.learning.application.impl.PromoteModelServiceImpl
import team.cklob.arena.domain.learning.domain.entity.ModelVersion
import team.cklob.arena.domain.learning.domain.repository.ModelVersionRepository
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import team.cklob.arena.global.exception.ExpectedException
import java.time.LocalDateTime

class PromoteModelServiceImplTest : DescribeSpec({
    val repository = mockk<ModelVersionRepository>()
    val service = PromoteModelServiceImpl(repository)

    it("Challenger를 승격하고 기존 Champion을 퇴역시킨다") {
        val champion = model(1L, ModelStatus.CHAMPION)
        val challenger = model(2L, ModelStatus.CHALLENGER)
        every { repository.findAllForUpdate() } returns listOf(champion, challenger)

        val result = service.execute(2L)

        result.status shouldBe ModelStatus.CHAMPION
        champion.status shouldBe ModelStatus.RETIRED
        challenger.status shouldBe ModelStatus.CHAMPION
    }

    it("Challenger가 아닌 모델은 승격하지 않는다") {
        every { repository.findAllForUpdate() } returns listOf(model(1L, ModelStatus.CHAMPION))

        shouldThrow<ExpectedException> { service.execute(1L) }.errorCode shouldBe LearningErrorCode.MODEL_STATUS_CONFLICT
    }

    it("존재하지 않는 모델은 404 오류로 처리한다") {
        every { repository.findAllForUpdate() } returns emptyList()

        shouldThrow<ExpectedException> { service.execute(99L) }.errorCode shouldBe LearningErrorCode.MODEL_VERSION_NOT_FOUND
    }
}) {
    companion object {
        private fun model(
            id: Long,
            status: ModelStatus,
        ) = ModelVersion("v$id", LocalDateTime.of(2026, 8, 1, 0, 0), "{}", status).apply { this.id = id }
    }
}
