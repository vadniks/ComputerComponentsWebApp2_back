package com.example.plugins

import com.example.db.ComponentsRepo
import com.example.db.models.ID
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*

fun Application.configureRouting() {

    fun PipelineContext<Unit, ApplicationCall>.getId() = call.parameters.getOrFail<Int>(ID).toInt()

    /*
    * curl 0.0.0.0:8080/component -X POST --header "Content-Type: application/json" --data '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":"null"}'
    **/
    routing {
        route("/component") {
            post { ComponentsRepo.addIfNotExists(call.receive()) }
            get { call.respond(ComponentsRepo.getAll()) }
            route("/{id}") {
                get { call.respondNullable(ComponentsRepo.getBy(getId())) }
                put { ComponentsRepo.update(getId(), call.receive()) }
                delete { ComponentsRepo.delete(getId()) }
            }
        }
        static {
            resource("/", "/static/index.html")
            static("/") {
                resources("static")
            }
        }
    }
}
