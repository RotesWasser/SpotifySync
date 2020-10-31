package com.roteswasser.spotifysync.frontend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.roteswasser.spotifysync.common.ISpotifyConnection
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SpotifyConnectionBuilder(
        @Autowired private val oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager,
        @Value("\${spotifysync.apiendpoint}") private val apiEndpoint: String) {

    private val client = WebClient.builder()
            .exchangeStrategies(
                    ExchangeStrategies.builder()
                            .codecs { configurer: ClientCodecConfigurer ->
                                configurer
                                        .defaultCodecs()
                                        .maxInMemorySize(16 * 1024 * 1024)
                            }
                            .build()
            )
            .baseUrl(apiEndpoint)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    fun getClient(principalName: String): SpotifyConnection {
        return SpotifyConnection(oAuth2AuthorizedClientManager, client, principalName)
    }
}

class SpotifyConnection(
        private val oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager,
        private val client: WebClient,
        private val principalName: String
) : ISpotifyConnection {

    // region Saved Songs Operations
    override fun getMySavedSongs(limit: Int) = getPaginatedItems<ISpotifyConnection.SavedTrack, ISpotifyConnection.PaginatedResponse<ISpotifyConnection.SavedTrack>>(principalName, limit, "/me/tracks?offset=%d&limit=50")
    // endregion

    // region Playlist Management Operations
    override fun getMyPlaylists(): List<ISpotifyConnection.Playlist> = getAllPaginatedItems(principalName, "/me/playlists?offset=0&limit=20")

    override fun getPlaylist(playlistId: String): ISpotifyConnection.Playlist = executeRequest(oAuth2AuthorizedClientManager, principalName, "/playlists/${playlistId}", null, HttpMethod.GET)

    override fun createPlaylistForMyself(
            playlistName: String,
            playlistDescription: String,
            public: Boolean,
            collaborative: Boolean
    ) = createPlaylistForUser(principalName, playlistName, playlistDescription, public, collaborative)

    override fun createPlaylistForUser(
            userId: String,
            playlistName: String,
            playlistDescription: String,
            public: Boolean,
            collaborative: Boolean
    ): ISpotifyConnection.Playlist {
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
    override fun getPlaylistItems(playlistId: String) = getAllPaginatedItems<ISpotifyConnection.PlaylistItem, ISpotifyConnection.PaginatedResponse<ISpotifyConnection.PlaylistItem>>(principalName, "/playlists/${playlistId}/tracks")

    override fun deletePlaylistItems(playlistId: String, deletions: List<ISpotifyConnection.TrackDeletion>, snapshot_id: String?) = executeRequest<ISpotifyConnection.PlaylistUpdateResponse>(
            oAuth2AuthorizedClientManager,
            principalName,
            "/playlists/${playlistId}/tracks",
            jacksonObjectMapper().writeValueAsString(ISpotifyConnection.DeletionRequest(snapshot_id, deletions)),
            HttpMethod.DELETE
    )

    override fun addPlaylistItems(playlistId: String, position: Int?, urisToAdd: List<String>) = executeRequest<String>(
            oAuth2AuthorizedClientManager,
            principalName,
            "playlists/${playlistId}/tracks",
            jacksonObjectMapper().writeValueAsString(ISpotifyConnection.AdditionRequest(position, urisToAdd)),
            HttpMethod.POST
    )

    override fun replacePlaylistItems(
            playlistId: String,
            itemURIs: List<String>
    ): ISpotifyConnection.PlaylistUpdateResponse {
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
    private inline fun <reified InnerItemType, reified PaginatedType : ISpotifyConnection.PaginatedResponse<InnerItemType>> getPaginatedItems(
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

    private inline fun <reified InnerItemType, reified PaginatedType : ISpotifyConnection.PaginatedResponse<InnerItemType>> getAllPaginatedItems(
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

        if (response.statusCode() !in listOf(HttpStatus.OK, HttpStatus.CREATED)) {
            val statusCode = response.statusCode()
            val bodyAsString = response.bodyToMono<String>().block()!!
            throw ISpotifyConnection.ConnectionException("Failure during request", bodyAsString)
        }

        return response.bodyToMono<T>().block()!!
    }


    // endregion
}