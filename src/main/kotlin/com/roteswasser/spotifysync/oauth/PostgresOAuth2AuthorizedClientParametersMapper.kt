package com.roteswasser.spotifysync.oauth

import org.springframework.jdbc.core.SqlParameterValue
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.sql.Types
import java.util.ArrayList
import java.util.function.Function

class PostgresOAuth2AuthorizedClientParametersMapper : Function<JdbcOAuth2AuthorizedClientService.OAuth2AuthorizedClientHolder, List<SqlParameterValue>> {
    override fun apply(authorizedClientHolder: JdbcOAuth2AuthorizedClientService.OAuth2AuthorizedClientHolder): List<SqlParameterValue> {
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