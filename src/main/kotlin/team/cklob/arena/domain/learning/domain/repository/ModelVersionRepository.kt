package team.cklob.arena.domain.learning.domain.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import team.cklob.arena.domain.learning.domain.entity.ModelVersion
import team.cklob.arena.domain.learning.domain.type.ModelStatus

interface ModelVersionRepository : JpaRepository<ModelVersion, Long> {
    fun findAllByOrderByTrainedAtDesc(): List<ModelVersion>

    fun findAllByStatusOrderByTrainedAtDesc(status: ModelStatus): List<ModelVersion>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select model from ModelVersion model order by model.id")
    fun findAllForUpdate(): List<ModelVersion>
}
