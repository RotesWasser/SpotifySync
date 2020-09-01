package com.roteswasser.spotifysync

import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import org.springframework.http.*
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.time.Instant


class SpotifyConnection(private val oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager) {

    // region Saved Songs Operations
    fun getMySavedSongs(principalName: String, limit: Int): List<SavedTrack> {
        val fetchedItems = mutableListOf<SavedTrack>()

        var offset: Int = 0
        var total: Int = 0

        do {
            val response = executeRequest<PaginatedResponse<SavedTrack>>(
                    oAuth2AuthorizedClientManager,
                    principalName,
                    "https://api.spotify.com/v1/me/tracks?offset=${offset}&limit=50",
                    null,
                    HttpMethod.GET
            ).body!!

            total = response.total
            offset += response.items.count()
            fetchedItems.addAll(response.items.take(limit - fetchedItems.count()))

        } while (fetchedItems.count() < limit && fetchedItems.count() < total)

        return fetchedItems
    }

    // endregion

    // region Playlist Operations

    fun getMyPlaylists(principalName: String): List<Playlist> {
        val fetchedItems = mutableListOf<Playlist>()
        var nextURL: String? = "https://api.spotify.com/v1/me/playlists?offset=0&limit=20"

        do {
            val response = executeRequest<PaginatedResponse<Playlist>>(
                    oAuth2AuthorizedClientManager,
                    principalName,
                    nextURL!!,
                    null,
                    HttpMethod.GET).body!!

            fetchedItems.addAll(response.items)
            nextURL = response.next

        } while (nextURL != null)

        return fetchedItems
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

    fun replacePlaylistItems(
            principalName: String,
            playlistId: String,
            itemURIs: List<String>
    ): PlaylistUpdateResponse {
        if (itemURIs.count() > 100)
            throw IllegalArgumentException("Spotify API only allows setting 100 items with replace.")

        val requestBody = JSONObject().apply {
            val urisArray = JSONArray().apply {
                addAll(itemURIs)
            }

            put("uris", urisArray)
        }

        return executeRequest<PlaylistUpdateResponse>(
                oAuth2AuthorizedClientManager,
                principalName,
                "https://api.spotify.com/v1/playlists/${playlistId}/tracks",
                requestBody,
                HttpMethod.PUT
        ).body!!
    }

    // endregion

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
            val items: List<T>,
            val next: String?,
            val offset: Int,
            val previous: String?,
            val total: Int
    )

    data class Playlist(
            val collaborative: Boolean,
            val id: String,
            val name: String
    )

    data class SavedTrack(
            val added_at: Instant,
            val track: Track
    )

    data class Track(
            val uri: String
    )

    data class PlaylistUpdateResponse(
            val snapshot_id: String
    )

    class SpotifyCredentialsException(message: String?) : Exception(message) {

    }
}