package team.cklob.arena.domain.market.application.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import team.cklob.arena.domain.market.application.MarketDataClient
import team.cklob.arena.domain.market.application.SyncSymbolsService
import team.cklob.arena.domain.market.domain.entity.Symbol
import team.cklob.arena.domain.market.domain.repository.SymbolRepository
import team.cklob.arena.domain.market.domain.type.MarketType

@Service
class SyncSymbolsServiceImpl(
    private val marketDataClient: MarketDataClient,
    private val symbolRepository: SymbolRepository,
    private val transactionTemplate: TransactionTemplate,
) : SyncSymbolsService {
    override fun execute() {
        val synchronized =
            marketDataClient.fetchSymbols()
                .map { it.copy(code = it.code.trim().uppercase(), name = it.name.trim()) }
                .filter { it.code.isNotEmpty() && it.name.isNotEmpty() }
                .distinctBy { it.market to it.code }
                .groupBy { it.market }

        require(synchronized[MarketType.US].orEmpty().isNotEmpty())
        require(synchronized[MarketType.COIN].orEmpty().isNotEmpty())

        transactionTemplate.executeWithoutResult {
            synchronized.forEach { (market, incoming) ->
                val existing = symbolRepository.findAllByMarket(market).associateBy(Symbol::code)
                val incomingCodes = incoming.mapTo(mutableSetOf()) { it.code }

                incoming.forEach { external ->
                    existing[external.code]?.synchronize(external.name)
                        ?: symbolRepository.save(Symbol(external.market, external.code, external.name))
                }
                existing.values.filterNot { it.code in incomingCodes }.forEach(Symbol::deactivate)
            }
        }
    }
}
