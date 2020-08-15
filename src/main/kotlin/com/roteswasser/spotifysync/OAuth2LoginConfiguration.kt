package com.roteswasser.spotifysync

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User

@EnableWebSecurity
class OAuth2LoginSecurityConfig(private val customUserService: SpotifySyncUserService) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http {
            oauth2Login {
                //authorizedClientService = userService
                userInfoEndpoint {
                    userService = customUserService as OAuth2UserService<OAuth2UserRequest, OAuth2User>
                    customUserType(OAuth2SpotifySyncUser::class.java, "spotify")
                }
            }
        }
    }
}