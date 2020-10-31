package com.roteswasser.spotifysync.common.dto

data class ManualSyncTriggerResponse(
        val playlistId: String,
        val success: Boolean
)