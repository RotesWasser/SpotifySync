package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.entities.SpotifySyncUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.stereotype.Component


@Component
class SpotifySyncUserService(
        private val userRepository: SpotifySyncUserRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2SpotifySyncUser> /*, OAuth2AuthorizedClientService*/ {

    private val defaultUserService = DefaultOAuth2UserService()

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2SpotifySyncUser {

        val result = defaultUserService.loadUser(userRequest)
        val lookupResult = userRepository.findById(result.name)

        val user = if (lookupResult.isPresent) {
            // We have a user, don't need to create one
            lookupResult.get()
        } else {
            // We don't have a user, create one
            val newUser = SpotifySyncUser(
                id = result.name,
                displayName = result.attributes["display_name"] as String,
                invalidSpotifyCredentials = false,
                syncJobs = emptySet())

            userRepository.save(newUser)
            newUser
        }

        return OAuth2SpotifySyncUser(result.authorities, result.attributes, userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName)
    }
}

class OAuth2SpotifySyncUser(authorities: MutableCollection<out GrantedAuthority>?,
                            attributes: MutableMap<String, Any>,
                            name: String) : DefaultOAuth2User(authorities,attributes,name) {

}