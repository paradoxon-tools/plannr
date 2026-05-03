package de.chennemann.plannr.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
