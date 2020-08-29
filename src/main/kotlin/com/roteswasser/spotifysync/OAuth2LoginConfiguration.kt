package com.roteswasser.spotifysync

import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.SqlParameterValue
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService.OAuth2AuthorizedClientHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.sql.Types
import java.util.*
import java.util.function.Function


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

    class PostgresOAuth2AuthorizedClientParametersMapper : Function<OAuth2AuthorizedClientHolder, List<SqlParameterValue>> {
        override fun apply(authorizedClientHolder: OAuth2AuthorizedClientHolder): List<SqlParameterValue> {
            val authorizedClient = authorizedClientHolder.authorizedClient
            val principal = authorizedClientHolder.principal
            val clientRegistration = authorizedClient.clientRegistration
            val accessToken = authorizedClient.accessToken
            val refreshToken = authorizedClient.refreshToken
            val parameters: MutableList<SqlParameterValue> = ArrayList()
            parameters.add(SqlParameterValue(
                    Types.VARCHAR, clientRegistration.registrationId))
            parameters.add(SqlParameterValue(
                    Types.VARCHAR, principal.name))
            parameters.add(SqlParameterValue(
                    Types.VARCHAR, accessToken.tokenType.value))
            parameters.add(SqlParameterValue(
                    Types.VARBINARY, accessToken.tokenValue.toByteArray(StandardCharsets.UTF_8)))
            parameters.add(SqlParameterValue(
                    Types.TIMESTAMP, Timestamp.from(accessToken.issuedAt)))
            parameters.add(SqlParameterValue(
                    Types.TIMESTAMP, Timestamp.from(accessToken.expiresAt)))
            var accessTokenScopes: String? = null
            if (!CollectionUtils.isEmpty(accessToken.scopes)) {
                accessTokenScopes = StringUtils.collectionToDelimitedString(accessToken.scopes, ",")
            }
            parameters.add(SqlParameterValue(
                    Types.VARCHAR, accessTokenScopes))
            var refreshTokenValue: ByteArray? = null
            var refreshTokenIssuedAt: Timestamp? = null
            if (refreshToken != null) {
                refreshTokenValue = refreshToken.tokenValue.toByteArray(StandardCharsets.UTF_8)
                if (refreshToken.issuedAt != null) {
                    refreshTokenIssuedAt = Timestamp.from(refreshToken.issuedAt)
                }
            }
            parameters.add(SqlParameterValue(
                    Types.VARBINARY, refreshTokenValue))
            parameters.add(SqlParameterValue(
                    Types.TIMESTAMP, refreshTokenIssuedAt))
            return parameters
        }
    }
}