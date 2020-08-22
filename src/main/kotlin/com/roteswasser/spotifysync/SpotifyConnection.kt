package com.roteswasser.spotifysync

import net.minidev.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange


class SpotifyConnection(private val oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager) {

    fun getPlaylistsOfUser(principalName: String, userName: String): List<Playlist> {
        val request = OAuth2AuthorizeRequest
                .withClientRegistrationId("spotify")
                .principal(principalName)
                .build()

        val authorizedClient = oAuth2AuthorizedClientManager.authorize(request)
                ?: throw Exception("Failed to get an authorized client")

        val restTemplate = RestTemplate()
        val httpHeaders = HttpHeaders()
        httpHeaders.add(HttpHeaders.AUTHORIZATION,
                "Bearer " + authorizedClient.accessToken.tokenValue)

        val entity = HttpEntity("", httpHeaders)

        val result = restTemplate.exchange<PaginatedResponse<Playlist>>("https://api.spotify.com/v1/users/${userName}/playlists", HttpMethod.GET, entity)

        // TODO: Handle pagination and errors
        return result.body!!.items
    }

    fun createPlaylistForUser(
            principalName: String,
            userId: String,
            playlistName: String,
            playlistDescription: String,
            public: Boolean = false,
            collaborative: Boolean = false
    ): Playlist {
        val request = OAuth2AuthorizeRequest
                .withClientRegistrationId("spotify")
                .principal(principalName)
                .build()

        val authorizedClient = oAuth2AuthorizedClientManager.authorize(request)
                ?: throw Exception("Failed to get an authorized client")

        val restTemplate = RestTemplate()
        val httpHeaders = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.accessToken.tokenValue)
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        val playlistJsonObject = JSONObject().apply {
            put("name", playlistName)
            put("public", public)
            put("collaborative", collaborative)
            put("description", playlistDescription)
        }

        val entity = HttpEntity(playlistJsonObject.toJSONString(), httpHeaders)

        val result = restTemplate.exchange<Playlist>("https://api.spotify.com/v1/users/${userId}/playlists", HttpMethod.POST, entity)

        // TODO: Error handling!

        return result.body!!
    }

    data class PaginatedResponse<T>(
            val href: String,
            val items: List<T>
    )

    data class Playlist(
            val collaborative: Boolean,
            val id: String,
            val name: String
    )
}