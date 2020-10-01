package com.roteswasser.spotifysync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager

@SpringBootApplication
@EnableScheduling
class SpotifysyncApplication {
}

fun main(args: Array<String>) {
    runApplication<SpotifysyncApplication>(*args)
}