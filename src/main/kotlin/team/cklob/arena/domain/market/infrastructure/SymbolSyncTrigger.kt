package team.cklob.arena.domain.market.infrastructure

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import team.cklob.arena.domain.market.application.SyncSymbolsService
import team.cklob.arena.domain.market.domain.repository.SymbolRepository

@Component
@ConditionalOnProperty(prefix = "external.twelve-data", name = ["enabled"], havingValue = "true")
class SymbolSyncTrigger(
    private val syncSymbolsService: SyncSymbolsService,
    private val symbolRepository: SymbolRepository,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (symbolRepository.count() == 0L) synchronize()
    }

    @Scheduled(cron = "\${market.sync.cron:0 0 4 * * *}", zone = "\${market.sync.zone:Asia/Seoul}")
    fun synchronize() {
        runCatching(syncSymbolsService::execute)
            .onFailure { log.error("Symbol synchronization failed", it) }
    }
}
