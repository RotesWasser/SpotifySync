package com.roteswasser.spotifysync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpotifysyncApplication

fun main(args: Array<String>) {
    runApplication<SpotifysyncApplication>(*args)
}
