package com.example.plugins

import com.example.db.ComponentsRepo
import com.example.models.Component
import com.example.models.ID
import com.example.models.toJson
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*

fun Application.configureRouting() {

    /*
    * curl 0.0.0.0:8080/component -X POST --header "Content-Type: application/json" --data '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":"null"}'
    * */
    routing {
        route("/component") {
            post { ComponentsRepo.addComponentIfNotExists(call.receive()) }
            get { call.respond(ComponentsRepo.getComponents()) }
            get("/{id}") { call.respondNullable(
                ComponentsRepo.getComponent(call.parameters.getOrFail(ID).toIntOrNull() ?: 0)) }
        }
        static {
            resource("/", "/static/index.html")
            static("/") {
                resources("static")
            }
        }
    }
}
