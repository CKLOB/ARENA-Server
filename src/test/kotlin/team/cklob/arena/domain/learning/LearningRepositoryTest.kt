package team.cklob.arena.domain.learning

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import team.cklob.arena.domain.learning.domain.entity.DecisionLog
import team.cklob.arena.domain.learning.domain.entity.ModelVersion
import team.cklob.arena.domain.learning.domain.repository.DecisionLogRepository
import team.cklob.arena.domain.learning.domain.repository.ModelVersionRepository
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import team.cklob.arena.domain.learning.domain.type.TradingAction
import team.cklob.arena.domain.market.domain.type.MarketType
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
class LearningRepositoryTest(
    private val decisionLogRepository: DecisionLogRepository,
    private val modelVersionRepository: ModelVersionRepository,
) : DescribeSpec({
        extension(SpringExtension)

        beforeEach {
            decisionLogRepository.deleteAll()
            modelVersionRepository.deleteAll()
        }

        it("결정 로그를 복합 조건으로 최신순 조회한다") {
            decisionLogRepository.saveAll(
                listOf(
                    decision(1L, MarketType.US, "v1", LocalDateTime.of(2026, 8, 1, 10, 0)),
                    decision(1L, MarketType.US, "v1", LocalDateTime.of(2026, 8, 1, 11, 0)),
                    decision(2L, MarketType.COIN, "v2", LocalDateTime.of(2026, 8, 1, 12, 0)),
                ),
            )

            val result = decisionLogRepository.findAllByFilters(1L, MarketType.US, "v1")

            result shouldHaveSize 2
            result.map(DecisionLog::decidedAt) shouldBe result.map(DecisionLog::decidedAt).sortedDescending()
        }

        it("모델 버전을 상태로 필터링해 학습 최신순 조회한다") {
            modelVersionRepository.saveAll(
                listOf(
                    model("v1", ModelStatus.CHAMPION, LocalDateTime.of(2026, 7, 1, 0, 0)),
                    model("v2", ModelStatus.CHALLENGER, LocalDateTime.of(2026, 8, 1, 0, 0)),
                    model("v3", ModelStatus.CHALLENGER, LocalDateTime.of(2026, 8, 2, 0, 0)),
                ),
            )

            modelVersionRepository.findAllByStatusOrderByTrainedAtDesc(ModelStatus.CHALLENGER)
                .map(ModelVersion::versionTag) shouldBe listOf("v3", "v2")
        }
    }) {
    override fun extensions() = listOf(SpringExtension)

    companion object {
        private fun decision(
            challengeId: Long,
            market: MarketType,
            modelVersion: String,
            decidedAt: LocalDateTime,
        ) = DecisionLog(
            challengeId,
            "AAPL",
            market,
            TradingAction.BUY,
            BigDecimal("0.7500"),
            modelVersion,
            decidedAt,
        )

        private fun model(
            versionTag: String,
            status: ModelStatus,
            trainedAt: LocalDateTime,
        ) = ModelVersion(versionTag, trainedAt, "{\"accuracy\":0.8}", status)
    }
}
