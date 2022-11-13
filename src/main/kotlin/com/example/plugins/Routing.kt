package com.example.plugins

import com.example.db.models.ID
import com.example.service.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*

fun ApplicationCall.getIdParameter() = parameters[ID]?.toIntOrNull()
suspend fun ApplicationCall.respondOk() = respond(HttpStatusCode.OK)
suspend fun ApplicationCall.respondUserError() = respond(HttpStatusCode.BadRequest)
private fun Route.authAdmin(build: Route.() -> Unit) = authenticate(SESSION_ADMIN, build = build)
private fun Route.authAny(build: Route.() -> Unit) = authenticate(SESSION_USER, SESSION_ADMIN, build = build)
suspend fun ApplicationCall.respondOkIfTrue(result: Boolean) = if (result) respondOk() else respondUserError()

suspend inline fun ApplicationCall.doIfUserIdFound(crossinline action: suspend (Int) -> Unit)
= sessions.get<UserIdPrincipal>()?.id.apply { if (this != null) action(this) else respondUserError() }

suspend inline fun ApplicationCall.doIfIdParameterIsNotNull(crossinline action: suspend (Int) -> Unit)
= getIdParameter().apply { if (this != null) action(this) else respondUserError() }

suspend inline fun <reified T> ApplicationCall.respondIfIdParameterIsNotNull(crossinline responseMaker: suspend (Int) -> T)
= getIdParameter().apply { respondNullable(if (this != null) responseMaker(this) else null) }

suspend inline fun ApplicationCall.respondOkIfTrueWithIdParameter(crossinline responseMaker: suspend (Int) -> Boolean)
= doIfIdParameterIsNotNull { respondOkIfTrue(responseMaker(it)) }

private typealias Pipeline = PipelineContext<Unit, ApplicationCall>
private val Pipeline.componentService get() = call.service(ComponentService)
private val Pipeline.userService get() = call.service(UserService)
private const val idParam = "/{id}"

/**
 * curl 0.0.0.0:8080/component
 * curl 0.0.0.0:8080/component/1
 * curl 0.0.0.0:8080/component -X POST -H "Content-Type: application/json" -d '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}' -b cookie.txt
 * curl 0.0.0.0:8080/component/2 -X PUT -H "Content-Type: application/json" -d '{"id":2,"title":"a","type":"MB","description":"b","cost":111,"image":null}' -b cookie.txt
 * curl 0.0.0.0:8080/component/2 -X DELETE -b cookie.txt
 * curl 0.0.0.0:8080/login -X POST -F 'name=user' -F 'password=user' -c cookie.txt
 * curl 0.0.0.0:8080/login -X POST -F 'name=admin' -F 'password=admin' -c cookie.txt
 * curl 0.0.0.0:8080/logout -X POST -b cookie.txt -c cookie.txt
 * curl 0.0.0.0:8080/selected -b cookie.txt
 * curl 0.0.0.0:8080/select/1 -X POST -b cookie.txt
 * curl 0.0.0.0:8080/clearSelected -X POST -b cookie.txt
 * curl 0.0.0.0:8080/user -X POST -H "Content-Type: application/json" -d '{"id":3,"name":"test","role":"USER","password":"test","firstName":null,"lastName":null,"phone":null,"address":null,"selection":null}' -b cookie.txt
 * curl 0.0.0.0:8080/user -b cookie.txt
 * curl 0.0.0.0:8080/user/3 -b cookie.txt
 * curl 0.0.0.0:8080/user/3 -X PUT -H "Content-Type: application/json" -d '{"id":3,"name":"test","role":"USER","password":"pass","firstName":null,"lastName":null,"phone":null,"address":null,"selection":null}' -b cookie.txt
 * curl 0.0.0.0:8080/user/3 -X DELETE -b cookie.txt
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
    authAdmin { post { componentService.add() } }
    get { componentService.getAll() }
    route(idParam) {
        get { componentService.getById() }
        authAdmin { put { componentService.update() } }
        authAdmin { delete { componentService.delete() } }
    }
}

private fun Routing.userRouting() {
    authenticate(AUTH_FORM) { post("/login") { userService.login() } }
    authAny {
        post("/logout") { userService.logout() }
        post("/select") { userService.select() }
        get("/selected") { userService.selected() }
        post("/clearSelected") { userService.clearSelected() }
        post("/order") { userService.order() }
    }
    authAdmin { route("/user") {
        post { userService.add() }
        get { userService.getAll() }
        route(idParam) {
            get { userService.getById() }
            put { userService.update() }
            delete { userService.delete() }
        }
    } }
}
