package com.roteswasser.spotifysync.syncservice

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.roteswasser.spotifysync.common.repositories"])
@EntityScan(basePackages = ["com.roteswasser.spotifysync.common.entities"])
class FrontendConfig {
}