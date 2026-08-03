package team.cklob.arena.domain.market.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.cklob.arena.domain.market.domain.entity.PriceSnapshot

interface PriceSnapshotRepository : JpaRepository<PriceSnapshot, Long>
