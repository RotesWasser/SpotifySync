package com.roteswasser.spotifysync.controllers

import com.roteswasser.spotifysync.oauth.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.repositories.SpotifySyncUserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(userRepository: SpotifySyncUserRepository) : SpotifySyncCustomController(userRepository) {

    @GetMapping("/")
    fun demoMainPage(model: Model): String {

        populateHeaderFields(model)

        return "index"
    }

}