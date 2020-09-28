package com.roteswasser.spotifysync.oauth

import com.roteswasser.spotifysync.entities.SpotifySyncUser
import com.roteswasser.spotifysync.oauth.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.repositories.SpotifySyncUserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.stereotype.Component


@Component
class SpotifySyncUserService(
        private val userRepository: SpotifySyncUserRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2SpotifySyncUser> {

    private val defaultUserService = DefaultOAuth2UserService()

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2SpotifySyncUser {

        val result = defaultUserService.loadUser(userRequest)
        val lookupResult = userRepository.findById(result.name)

        if (!lookupResult.isPresent) {
            // We don't know this user, create them in the database.
            val newUser = SpotifySyncUser(
                id = result.name,
                displayName = result.attributes["display_name"] as String,
                invalidSpotifyCredentials = false,
                syncJobs = emptySet())

            userRepository.save(newUser)
        }

        return OAuth2SpotifySyncUser(result.authorities, result.attributes, userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName)
    }
}

