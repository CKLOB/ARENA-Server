package team.cklob.arena.domain.market.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.type.MarketType

interface SymbolRepository : JpaRepository<Symbol, Long> {
    fun findAllByMarketAndIsActiveTrue(
        market: MarketType,
        pageable: Pageable,
    ): Page<Symbol>

    @Query(
        """
        SELECT symbol
        FROM Symbol symbol
        WHERE symbol.market = :market
          AND symbol.isActive = true
          AND (LOWER(symbol.code) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\'
            OR LOWER(symbol.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\')
        """,
    )
    fun searchActive(
        @Param("market") market: MarketType,
        @Param("keyword") keyword: String,
        pageable: Pageable,
    ): Page<Symbol>

    fun findAllByMarket(market: MarketType): List<Symbol>
}
