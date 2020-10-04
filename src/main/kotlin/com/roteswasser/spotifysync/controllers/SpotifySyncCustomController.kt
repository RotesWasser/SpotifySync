package com.roteswasser.spotifysync.controllers

import com.roteswasser.spotifysync.oauth.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.repositories.SpotifySyncUserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.Model
import org.springframework.ui.set

open class SpotifySyncCustomController(protected val userRepository: SpotifySyncUserRepository) {
    fun populateHeaderFields(model: Model) {
        when (val principal = SecurityContextHolder.getContext().authentication.principal) {
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
    }
}