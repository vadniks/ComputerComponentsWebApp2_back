package com.example.plugins

import com.example.db.models.ID
import com.example.service.ComponentService
import com.example.service.UserService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

fun ApplicationCall.getIdParameter() = parameters.getOrFail<Int>(ID).toInt()
suspend fun ApplicationCall.respondOk() = respond(HttpStatusCode.OK)
suspend fun ApplicationCall.respondUserError() = respond(HttpStatusCode.BadRequest)
private fun Route.authAdmin(build: Route.() -> Unit) = authenticate(SESSION_ADMIN, build = build)
private fun Route.authUser(build: Route.() -> Unit) = authenticate(SESSION_USER, build = build)
private fun Route.authAny(build: Route.() -> Unit) = authenticate(SESSION_USER, SESSION_ADMIN, build = build)

suspend fun ApplicationCall.doIfTokenIsNotNull(
    onNull: (suspend () -> Unit)? = null, action: suspend (String) -> Unit
) {
    val token = sessions.get<UserTokenPrincipal>()?.token
    if (token != null) { action(token); respondOk() }
    else if (onNull != null) onNull() else respondUserError()
}

suspend inline fun <reified T>
ApplicationCall.respondIfTokenIsNotNull(crossinline responseMaker: suspend (String) -> T) {
    val token = sessions.get<UserTokenPrincipal>()?.token
    respondNullable(if (token != null) responseMaker(token) else null)
}

/**
 * curl 0.0.0.0:8080/component
 * curl 0.0.0.0:8080/component/1
 * curl 0.0.0.0:8080/component -X POST -H "Content-Type: application/json" -d '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}' -b cookie.txt
 * curl 0.0.0.0:8080/component/2 -X PUT -H "Content-Type: application/json" -d '{"id":2,"title":"a","type":"MB","description":"b","cost":111,"image":null}' -b cookie.txt
 * curl 0.0.0.0:8080/component/2 -X DELETE -b cookie.txt
 * curl 0.0.0.0:8080/login -X POST -F 'name=user' -F 'password=user' -c cookie.txt
 * curl 0.0.0.0:8080/login -X POST -F 'name=admin' -F 'password=admin' -c cookie.txt
 */
fun Application.configureRouting() = routing {
    componentRouting()
    userRouting()
    staticRouting()
}

private fun Routing.staticRouting() = static {
    resource("/", "/static/index.html")
    static("/") {
        resources("static")
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
}

private fun Routing.userRouting() {
    authenticate(FORM) { post("/login") { UserService.login(call) } }
    authAny { post("/logout") { UserService.logout(call) } }
    authAny { post("/select/{id}") { UserService.select(call) } }
    authAny { get("/selected") { UserService.selected(call) } }
    authAny { post("/clearSelected") { UserService.clearSelected(call) } }
}
