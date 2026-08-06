package team.cklob.arena.domain.learning.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.cklob.arena.domain.learning.LearningErrorCode
import team.cklob.arena.domain.learning.application.impl.GetShapExplanationServiceImpl
import team.cklob.arena.domain.learning.application.result.ShapExplanationResult
import team.cklob.arena.domain.learning.domain.repository.DecisionLogRepository
import team.cklob.arena.global.exception.ExpectedException
import java.math.BigDecimal

class GetShapExplanationServiceImplTest : DescribeSpec({
    val repository = mockk<DecisionLogRepository>()
    val client = mockk<ObservabilityClient>()
    val service = GetShapExplanationServiceImpl(repository, client)

    beforeEach {
        clearMocks(repository, client)
    }

    it("결정 로그가 존재하면 외부 SHAP 설명을 반환한다") {
        val expected = ShapExplanationResult(1L, BigDecimal("0.5"), emptyList(), BigDecimal("0.7"))
        every { repository.existsById(1L) } returns true
        every { client.getShapExplanation(1L) } returns expected

        service.execute(1L) shouldBe expected
    }

    it("결정 로그가 없으면 외부 API를 호출하지 않는다") {
        every { repository.existsById(1L) } returns false

        shouldThrow<ExpectedException> { service.execute(1L) }.errorCode shouldBe LearningErrorCode.DECISION_LOG_NOT_FOUND
        verify(exactly = 0) { client.getShapExplanation(any()) }
    }
})
