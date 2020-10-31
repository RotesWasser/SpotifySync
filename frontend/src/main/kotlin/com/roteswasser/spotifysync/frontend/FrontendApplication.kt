package com.roteswasser.spotifysync.frontend

import com.roteswasser.spotifysync.frontend.synchronization.SyncTrigger
import com.roteswasser.spotifysync.frontend.synchronization.home.HomeSyncTrigger
import com.roteswasser.spotifysync.frontend.synchronization.home.ScheduledJobs
import com.roteswasser.spotifysync.frontend.synchronization.hosted.HostedSyncTrigger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FrontendApplication {

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
    fun hostedSyncTrigger(@Value("\${spotifysync.syncservicebaseurl}") baseURL: String): SyncTrigger
            = HostedSyncTrigger(baseURL)


}

fun main(args: Array<String>) {
    runApplication<FrontendApplication>(*args)
}