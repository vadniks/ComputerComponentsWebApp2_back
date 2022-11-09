package com.example.plugins

import com.example.db.repo.ComponentsRepo
import com.example.db.models.ID
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*

fun Application.configureRouting() {

    fun PipelineContext<Unit, ApplicationCall>.getId() = call.parameters.getOrFail<Int>(ID).toInt()
    suspend fun PipelineContext<Unit, ApplicationCall>.respondOk() = call.respond(HttpStatusCode.OK)
    fun Route.authAdmin(build: Route.() -> Unit) = authenticate(SESSION_ADMIN, build = build)

    /*
     * curl 0.0.0.0:8080/component
     * curl 0.0.0.0:8080/component/1
     * curl 0.0.0.0:8080/component -X POST --header "Content-Type: application/json" --data '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}'
     * curl 0.0.0.0:8080/component/1 -X PUT --header "Content-Type: application/json" --data '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}'
     * curl 0.0.0.0:8080/component/2 -X DELETE
     * curl -v 0.0.0.0:8080/login -X POST -F 'name=admin' -F 'password=admin'
     **/
    routing {
        route("/component") {
            authAdmin { post { ComponentsRepo.addIfNotExists(call.receive()) } }
            get { call.respond(ComponentsRepo.getAll()) }
            route("/{id}") {
                get { call.respondNullable(ComponentsRepo.getBy(getId())) }
                authAdmin { put { ComponentsRepo.update(getId(), call.receive()) } }
                authAdmin { delete { ComponentsRepo.delete(getId()) } }
            }
        }
        authenticate(FORM) { post("/login") {
            call.sessions.set(call.principal<UserTokenPrincipal>()!!)
            respondOk()
//            call.respondRedirect("/")
        } }
        authenticate(SESSION_USER, SESSION_ADMIN) { post("/logout") {
            call.sessions.clear<UserTokenPrincipal>()
            respondOk()
        } }
        static {
            resource("/", "/static/index.html")
            static("/") {
                resources("static")
            }
        }
    }
}
