package com.roteswasser.spotifysync.common.repositories

import com.roteswasser.spotifysync.common.entities.SpotifySyncUser
import org.springframework.data.repository.CrudRepository

interface SpotifySyncUserRepository : CrudRepository<SpotifySyncUser, String>