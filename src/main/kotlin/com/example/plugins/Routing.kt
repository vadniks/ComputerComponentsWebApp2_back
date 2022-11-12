package com.example.plugins

import com.example.db.repo.ComponentsRepo
import com.example.db.models.ID
import com.example.service.ComponentService
import com.example.service.UserService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

fun getId(call: ApplicationCall) = call.parameters.getOrFail<Int>(ID).toInt()
suspend fun respondOk(call: ApplicationCall) = call.respond(HttpStatusCode.OK)
fun Route.authAdmin(build: Route.() -> Unit) = authenticate(SESSION_ADMIN, build = build)

/**
 * curl 0.0.0.0:8080/component
 * curl 0.0.0.0:8080/component/1
 * curl 0.0.0.0:8080/component -X POST --header "Content-Type: application/json" --data '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}'
 * curl 0.0.0.0:8080/component/1 -X PUT --header "Content-Type: application/json" --data '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}'
 * curl 0.0.0.0:8080/component/2 -X DELETE
 * curl -v 0.0.0.0:8080/login -X POST -F 'name=admin' -F 'password=admin'
 */
fun Application.configureRouting() = routing {
    componentRouting()
    userRouting()

    static {
        resource("/", "/static/index.html")
        static("/") {
            resources("static")
        }
    }
}

private fun Routing.componentRouting() = route("/component") {
    authAdmin { post { ComponentService.add(call) } }
    get { ComponentService.getAll(call) }
    route("/{id}") {
        get { ComponentService.getById(call) }
        authAdmin { put { ComponentService.update(call) } }
        authAdmin { delete { ComponentService.delete(call) } }
    }

    post("/select/{id}") {

    }
}

private fun Routing.userRouting() {
    authenticate(FORM) { post("/login") { UserService.login(call) } }
    authenticate(SESSION_USER, SESSION_ADMIN) { post("/logout") { UserService.logout(call) } }
}
