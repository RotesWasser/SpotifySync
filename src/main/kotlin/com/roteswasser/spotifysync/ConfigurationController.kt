package com.roteswasser.spotifysync

import org.apache.tomcat.util.http.parser.Authorization
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import java.lang.Exception

@Controller
class ConfigurationController(
        private val userRepository: SpotifySyncUserRepository,
        private val syncJobRepository: SyncJobRepository
) {

    @GetMapping("/configuration")
    fun configDisplay(model: Model, authentication: Authentication): String {
        val principal = authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        val user = userRepository.findById(principal.name).get()



        model["displayName"] = user.displayName
        model["syncJobs"] = user.syncJobs

        return "configuration"
    }
}