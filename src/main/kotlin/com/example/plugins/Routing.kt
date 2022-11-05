package com.example.plugins

import com.example.db.ComponentsRepo
import com.example.models.ID
import com.example.models.toJson
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*

const val NULL = "null"

fun Application.configureRouting() {

    routing {
        get("/component/{id}") { call.respond(
            ComponentsRepo.getComponent(call.parameters.getOrFail(ID).toIntOrNull() ?: 0)?.toJson() ?: NULL) }

        static {
            resource("/", "/static/index.html")
            static("/") {
                resources("static")
            }
        }
    }
}
