package com.roteswasser.spotifysync.repositories

import com.roteswasser.spotifysync.entities.SpotifySyncUser
import org.springframework.data.repository.CrudRepository

interface SpotifySyncUserRepository : CrudRepository<SpotifySyncUser, String>