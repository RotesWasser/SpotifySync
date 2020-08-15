package com.roteswasser.spotifysync

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import java.lang.Exception

@Controller
class TestController(
        private val authorizedClientService: OAuth2AuthorizedClientService) {

    @GetMapping("/")
    fun demoMainPage(model: Model, authentication: Authentication): String {
        val authorizedClient = this.authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>("spotify", authentication.name)

        val principal = authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        model["displayName"] = principal.user.displayName

        return "index"
    }
}