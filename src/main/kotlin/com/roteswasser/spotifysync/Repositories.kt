package com.roteswasser.spotifysync

import org.springframework.data.repository.CrudRepository
import java.util.*

interface SpotifySyncUserRepository : CrudRepository<SpotifySyncUser, String> {

}