package com.roteswasser.spotifysync.oauth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class OAuth2SpotifySyncUser(authorities: MutableCollection<out GrantedAuthority>?,
                            attributes: MutableMap<String, Any>,
                            name: String) : DefaultOAuth2User(authorities,attributes,name) {

}