package com.roteswasser.spotifysync.frontend.controllers

import com.roteswasser.spotifysync.common.repositories.SpotifySyncUserRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(userRepository: SpotifySyncUserRepository) : SpotifySyncCustomController(userRepository) {

    @GetMapping("/")
    fun demoMainPage(model: Model): String {

        populateHeaderFields(model)

        return "index"
    }

}