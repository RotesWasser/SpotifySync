package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.synchronization.SyncTrigger
import com.roteswasser.spotifysync.synchronization.home.HomeSyncTrigger
import com.roteswasser.spotifysync.synchronization.home.ScheduledJobs
import com.roteswasser.spotifysync.synchronization.hosted.HostedSyncTrigger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SpotifysyncApplication () {

    @Bean
    @ConditionalOnProperty(
            prefix = "spotifysync",
            name = ["deployment"],
            havingValue = "home"
    )
    @ConditionalOnMissingBean
    fun homeSyncTrigger(@Autowired homeSynchronization: ScheduledJobs): SyncTrigger = HomeSyncTrigger(homeSynchronization)

    @Bean
    @ConditionalOnProperty(
            prefix = "spotifysync",
            name = ["deployment"],
            havingValue = "hosted"
    )
    @ConditionalOnMissingBean
    fun hostedSyncTrigger(): SyncTrigger = HostedSyncTrigger()
}

fun main(args: Array<String>) {
    runApplication<SpotifysyncApplication>(*args)
}