package team.cklob.arena.domain.learning.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import team.cklob.arena.domain.learning.domain.type.ModelStatus
import java.time.LocalDateTime

@Entity
@Table(
    name = "model_versions",
    uniqueConstraints = [UniqueConstraint(name = "uq_model_versions_version_tag", columnNames = ["version_tag"])],
)
class ModelVersion(
    @Column(name = "version_tag", nullable = false, length = 100)
    var versionTag: String,
    @Column(name = "trained_at", nullable = false)
    var trainedAt: LocalDateTime,
    @Column(name = "performance_metrics", nullable = false, columnDefinition = "TEXT")
    var performanceMetrics: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ModelStatus,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    fun promote() {
        status = ModelStatus.CHAMPION
    }

    fun retire() {
        status = ModelStatus.RETIRED
    }
}
