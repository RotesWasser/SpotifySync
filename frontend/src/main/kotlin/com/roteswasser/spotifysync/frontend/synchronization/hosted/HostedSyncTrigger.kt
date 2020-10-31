package com.roteswasser.spotifysync.frontend.synchronization.hosted

import com.roteswasser.spotifysync.common.dto.ManualSyncTriggerResponse
import com.roteswasser.spotifysync.common.entities.SyncJob
import com.roteswasser.spotifysync.frontend.SpotifyConnection
import com.roteswasser.spotifysync.frontend.synchronization.SyncTrigger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

class HostedSyncTrigger(
        @Value("\${spotifysync.syncservicebaseurl}") private val apiEndpoint: String
) : SyncTrigger {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val client = WebClient.builder()
            .baseUrl(apiEndpoint)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    override fun queueSync(toRun: SyncJob) {
        logger.debug("Requesting a manual sync of target id ${toRun.targetPlaylistId} from syncservice.")

        val response = client.method(HttpMethod.POST)
                .uri("/trigger/${toRun.targetPlaylistId}")
                .exchange()
                .block()!!

        if (response.statusCode() !in listOf(HttpStatus.OK, HttpStatus.CREATED)) {
            val statusCode = response.statusCode()
            val bodyAsString = response.bodyToMono<String>().block()!!
            throw SpotifyConnection.ConnectionException("Failure during request", bodyAsString)
        }

        val result = response.bodyToMono<ManualSyncTriggerResponse>().block()!!
    }
}