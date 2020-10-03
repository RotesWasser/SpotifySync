package com.roteswasser.spotifysync.oauth

import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User


@EnableWebSecurity
class OAuth2LoginSecurityConfig(private val customUserService: SpotifySyncUserService) : WebSecurityConfigurerAdapter() {

    @Bean
    fun oAuth2AuthorizedClientService(jdbcOperations: JdbcOperations, clientRegistrationRepository: ClientRegistrationRepository): JdbcOAuth2AuthorizedClientService {
        val service = JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository)
        service.setAuthorizedClientParametersMapper(PostgresOAuth2AuthorizedClientParametersMapper())
        return service
    }

    @Bean
    fun oAuth2AuthorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository,
                                      oAuth2AuthorizedClientService: JdbcOAuth2AuthorizedClientService): OAuth2AuthorizedClientManager {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .password()
                .build()

        val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
        manager.setAuthorizedClientProvider(authorizedClientProvider)
        return manager
    }

    override fun configure(http: HttpSecurity) {
        http {
            oauth2Login {
                //authorizedClientService = userService
                userInfoEndpoint {
                    userService = customUserService as OAuth2UserService<OAuth2UserRequest, OAuth2User>
                    customUserType(OAuth2SpotifySyncUser::class.java, "spotify")
                }

                loginPage = "/login"
            }

            anonymous {

            }

            authorizeRequests {
                authorize("/", "anonymous or authenticated")
                authorize("/login", anonymous)

                authorize(anyRequest, authenticated)
            }
        }
    }

}