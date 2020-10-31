package com.roteswasser.spotifysync.common

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

interface ISpotifyConnection {
    // region Saved Songs Operations
    fun getMySavedSongs(limit: Int): List<SavedTrack>

    // region Playlist Management Operations
    fun getMyPlaylists(): List<Playlist>
    fun getPlaylist(playlistId: String): Playlist
    fun createPlaylistForMyself(
            playlistName: String,
            playlistDescription: String,
            public: Boolean = false,
            collaborative: Boolean = false
    ): Playlist

    fun createPlaylistForUser(
            userId: String,
            playlistName: String,
            playlistDescription: String,
            public: Boolean = false,
            collaborative: Boolean = false
    ): Playlist

    // region Playlist Item Operations
    fun getPlaylistItems(playlistId: String): List<PlaylistItem>
    fun deletePlaylistItems(playlistId: String, deletions: List<TrackDeletion>, snapshot_id: String?): PlaylistUpdateResponse
    fun addPlaylistItems(playlistId: String, position: Int?, urisToAdd: List<String>): String
    fun replacePlaylistItems(
            playlistId: String,
            itemURIs: List<String>
    ): PlaylistUpdateResponse

    class ConnectionException(message: String, val body: String?) : Exception(message)

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
            val position: Int?,
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