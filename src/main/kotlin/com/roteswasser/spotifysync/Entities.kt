package com.roteswasser.spotifysync

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class SpotifySyncUser(
    @Id var id: String,
    var displayName: String
)