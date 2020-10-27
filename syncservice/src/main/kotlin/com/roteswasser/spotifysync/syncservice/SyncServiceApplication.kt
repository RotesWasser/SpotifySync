package com.roteswasser.spotifysync.syncservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SyncServiceApplication {
}

fun main(args: Array<String>) {
    runApplication<SyncServiceApplication>(*args)
}