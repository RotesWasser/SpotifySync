package com.roteswasser.spotifysync

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import org.springframework.http.*
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Instant


class SpotifyConnection(private val oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager) {

    private val client = WebClient.builder()
            .baseUrl("https://api.spotify.com/v1/")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    // region Saved Songs Operations
    fun getMySavedSongs(principalName: String, limit: Int)
        = getPaginatedItems<SavedTrack, PaginatedResponse<SavedTrack>>(principalName, limit, "/me/tracks?offset=%d&limit=50")
    // endregion

    // region Playlist Management Operations
    fun getMyPlaylists(principalName: String) : List<Playlist>
            = getAllPaginatedItems(principalName,"/me/playlists?offset=0&limit=20")

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
        return executeRequest(
                oAuth2AuthorizedClientManager,
                principalName,
                "/users/${userId}/playlists",
                playlistJsonObject,
                HttpMethod.POST
        )
    }

    // endregion


    // region Playlist Item Operations
    fun getPlaylistItems(principalName: String, playlistId: String)
        = getAllPaginatedItems<PlaylistItem, PaginatedResponse<PlaylistItem>>(principalName, "/playlists/${playlistId}/tracks")

    fun deletePlaylistItems(principalName: String, playlistId: String, deletions: List<TrackDeletion>, snapshot_id: String?)
        = executeRequest<PlaylistUpdateResponse>(
            oAuth2AuthorizedClientManager,
            principalName,
            "/playlists/${playlistId}/tracks",
            jacksonObjectMapper().writeValueAsString(DeletionRequest(snapshot_id, deletions)),
            HttpMethod.DELETE
        )

    fun addPlaylistItems(principalName: String, playlistId: String, position: Int, urisToAdd: List<String>, snapshot_id: String?)
        = executeRequest<PlaylistUpdateResponse>(
            oAuth2AuthorizedClientManager,
            principalName,
            "playlists/${playlistId}/tracks",
            jacksonObjectMapper().writeValueAsString(AdditionRequest(snapshot_id, position, urisToAdd)),
            HttpMethod.POST
        )

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

        return executeRequest(
                oAuth2AuthorizedClientManager,
                principalName,
                "/playlists/${playlistId}/tracks",
                requestBody,
                HttpMethod.PUT
        )
    }



    // endregion

    // region Request implementation
    private inline fun<reified InnerItemType, reified PaginatedType : PaginatedResponse<InnerItemType>> getPaginatedItems(
            principalName: String,
            limit: Int,
            urlFormatString: String
    ): List<InnerItemType> {
        val fetchedItems = mutableListOf<InnerItemType>()

        var offset = 0
        var total: Int

        do {
            val response = executeRequest<PaginatedType>(
                    oAuth2AuthorizedClientManager,
                    principalName,
                    urlFormatString.format(offset),
                    null,
                    HttpMethod.GET
            )

            total = response.total
            offset += response.items.count()
            fetchedItems.addAll(response.items.take(limit - fetchedItems.count()))

        } while (fetchedItems.count() < limit && fetchedItems.count() < total)

        return fetchedItems
    }

    private inline fun<reified InnerItemType, reified PaginatedType : PaginatedResponse<InnerItemType>> getAllPaginatedItems(
            principalName: String,
            startURL: String): List<InnerItemType> {
        val fetchedItems = mutableListOf<InnerItemType>()
        var nextURL: String? = startURL

        do {
            val response = executeRequest<PaginatedType>(
                    oAuth2AuthorizedClientManager,
                    principalName,
                    nextURL!!,
                    null,
                    HttpMethod.GET
            )

            fetchedItems.addAll(response.items)
            nextURL = response.next

        } while (nextURL != null)

        return fetchedItems
    }

    private inline fun <reified T : Any> executeRequest(
            oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager,
            principalName: String,
            url: String,
            body: Any?,
            httpMethod: HttpMethod
    ): T {
        val request = OAuth2AuthorizeRequest
                .withClientRegistrationId("spotify")
                .principal(principalName)
                .build()

        // 2020-08-29 Eirien187: "great, just great!"
        val authorizedClient = oAuth2AuthorizedClientManager.authorize(request)
                ?: throw Exception("Failed to get an authorized client")

        val webClientRequest = client.method(httpMethod)
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.accessToken.tokenValue)
                .let { if (body != null) it.body(BodyInserters.fromValue(body)) else it }

        val response = webClientRequest.exchange().block()!!

        return response.bodyToMono<T>().block()!!
    }


    // endregion



    data class PaginatedResponse<T>(
            val href: String,
            val items: List<T>,
            val next: String?,
            val offset: Int,
            val previous: String?,
            val total: Int,
            val limit: Int
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

    data class PlaylistItem(
            val track: Track
    )

    data class Track(
            val uri: String
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class DeletionRequest(
            val snapshot_id: String?,
            val tracks: List<TrackDeletion>
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class AdditionRequest(
            val snapshot_id: String?,
            val position: Int,
            val uris: List<String>
    )

    data class TrackDeletion(
            val uri: String,
            val positions: List<Int>
    )

    data class PlaylistUpdateResponse(
            val snapshot_id: String
    )

    class SpotifyCredentialsException(message: String?) : Exception(message)
}