package team.cklob.arena

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ArenaApplication

fun main(args: Array<String>) {
    runApplication<ArenaApplication>(*args)
}
