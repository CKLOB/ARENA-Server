package team.cklob.arena.domain.learning.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.cklob.arena.domain.learning.domain.entity.DecisionLog
import team.cklob.arena.domain.market.domain.type.MarketType

interface DecisionLogRepository : JpaRepository<DecisionLog, Long> {
    @Query(
        """
        select decision from DecisionLog decision
        where (:challengeId is null or decision.challengeId = :challengeId)
          and (:market is null or decision.market = :market)
          and (:modelVersion is null or decision.modelVersion = :modelVersion)
        order by decision.decidedAt desc
        """,
    )
    fun findAllByFilters(
        @Param("challengeId") challengeId: Long?,
        @Param("market") market: MarketType?,
        @Param("modelVersion") modelVersion: String?,
    ): List<DecisionLog>
}
