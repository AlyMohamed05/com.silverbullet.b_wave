package com.silverbullet.plugins

import com.silverbullet.core.events.configureEventsRoute
import com.silverbullet.feature_auth.route.configureAuthRoutes
import com.silverbullet.feature_connection.route.configureConnectionsRoutes
import com.silverbullet.feature_dm.route.configureDmMessagesRoutes
import com.silverbullet.feature_profile.route.configureProfileRoutes
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*

fun Application.configureRouting() {
    
    routing {

        configureEventsRoute()
        configureAuthRoutes()
        configureConnectionsRoutes()
        configureDmMessagesRoutes()
        configureProfileRoutes()
        static("/"){
            files("uploads")
        }
    }

}
