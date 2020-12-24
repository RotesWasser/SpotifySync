package com.roteswasser.spotifysync.syncservice

import com.roteswasser.spotifysync.common.repositories.SyncJobRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SyncServiceApplication {
    @Bean
    fun syncCoordinator(syncJobRepository: SyncJobRepository) : SyncCoordinator {
        return SyncCoordinator(syncJobRepository)
    }
}

fun main(args: Array<String>) {
    runApplication<SyncServiceApplication>(*args)
}