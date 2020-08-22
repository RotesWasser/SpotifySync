package com.roteswasser.spotifysync

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import java.lang.Exception

@Controller
class TestController(
        private val authorizedClientService: OAuth2AuthorizedClientService,
        private val userRepository: SpotifySyncUserRepository) {

    @GetMapping("/")
    fun demoMainPage(model: Model): String {

        when(val principal = SecurityContextHolder.getContext().authentication.principal) {
            is OAuth2SpotifySyncUser -> {
                val user = userRepository.findById(principal.name).get()

                model["isSignedIn"] = true
                model["displayName"] = user.displayName
                model["noSyncJobConfigured"] = user.syncJobs.isEmpty()
            }
            else -> {
                model["isSignedIn"] = false
            }
        }


        return "index"
    }
}