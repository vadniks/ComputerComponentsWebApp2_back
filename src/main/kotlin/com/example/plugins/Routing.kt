package com.example.plugins

import com.example.db.ComponentsRepo
import com.example.models.ID
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*

fun Application.configureRouting() {

    routing {
        get("/component/{id}") {
            ComponentsRepo.getComponent(call.parameters.getOrFail(ID))
        }

        static {
            resource("/", "/static/index.html")
            static("/") {
                resources("static")
            }
        }
    }
}
