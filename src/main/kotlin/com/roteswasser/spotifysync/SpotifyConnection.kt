package com.roteswasser.spotifysync

import net.minidev.json.JSONObject
import org.springframework.http.*
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange


class SpotifyConnection(private val oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager) {

    fun getMyPlaylists(principalName: String): List<Playlist> {
        return executeRequest<PaginatedResponse<Playlist>>(
                oAuth2AuthorizedClientManager,
                principalName,
                "https://api.spotify.com/v1/me/playlists?offset=0&limit=20",
                null,
                HttpMethod.GET).body!!.items
    }


    fun doesPlaylistExist(principalName: String, playlistId: String): Boolean {
        val response = executeRequest<Playlist>(
                oAuth2AuthorizedClientManager,
                principalName,
                "https://api.spotify.com/v1/playlists/${playlistId}",
                null,
                HttpMethod.GET
        )

        // TODO: For some reason, deleting a playlist in the spotify UI does not actually seem to
        // delete the playlist, it just seems to hide it from the users Playlist listing.
        // Maybe it is being deleted later on?
        return response.statusCode !in listOf(HttpStatus.NOT_FOUND)
    }


    fun createPlaylistForUser(
            principalName: String,
            userId: String,
            playlistName: String,
            playlistDescription: String,
            public: Boolean = false,
            collaborative: Boolean = false
    ): Playlist {
        val playlistJsonObject = JSONObject().apply {
            put("name", playlistName)
            put("public", public)
            put("collaborative", collaborative)
            put("description", playlistDescription)
        }

        // TODO: Error handling!
        return executeRequest<Playlist>(
                oAuth2AuthorizedClientManager,
                principalName,
                "https://api.spotify.com/v1/users/${userId}/playlists",
                playlistJsonObject,
                HttpMethod.POST).body!!
    }

    private inline fun <reified T> executeRequest(
            oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager,
            principalName: String,
            url: String,
            body: JSONObject?,
            httpMethod: HttpMethod
    ): ResponseEntity<T> {
        val request = OAuth2AuthorizeRequest
                .withClientRegistrationId("spotify")
                .principal(principalName)
                .build()

        // 2020-08-29 Eirien187: "great, just great!"
        val authorizedClient = oAuth2AuthorizedClientManager.authorize(request)
                ?: throw Exception("Failed to get an authorized client")

        val restTemplate = RestTemplate()

        val httpHeaders = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.accessToken.tokenValue)
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        val entity = HttpEntity(body?.toJSONString(), httpHeaders)

        return restTemplate.exchange<T>(url, httpMethod, entity)
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