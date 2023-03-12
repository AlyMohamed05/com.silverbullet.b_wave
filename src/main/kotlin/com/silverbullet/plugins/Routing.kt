package com.silverbullet.plugins

import com.silverbullet.feature_auth.route.configureAuthRoutes
import com.silverbullet.feature_connection.route.configureConnectionsRoutes
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    
    routing {

        configureAuthRoutes()
        configureConnectionsRoutes()
    }

}
