package com.silverbullet.feature_auth.route

import com.silverbullet.feature_auth.AuthController
import io.ktor.server.routing.*

fun Route.loginRoute(controller: AuthController){

    post("login") {

    }
}