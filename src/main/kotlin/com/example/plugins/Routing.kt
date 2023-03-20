package com.example.plugins

import com.example.db.models.ID
import com.example.db.models.TYPE
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
private fun Route.authUser(build: Route.() -> Unit) = authenticate(SESSION_USER, build = build)
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
private val Pipeline.sessionService get() = call.service(SessionService)
@Suppress("ConstPropertyName") private const val idParam = "/{id}"

/**
 * curl 0.0.0.0:8080/component
 * curl 0.0.0.0:8080/component/1
 * curl 0.0.0.0:8080/component -X POST -H "Content-Type: application/json" -d '{"id":2,"title":"a","type":"MB","description":"b","cost":200,"image":null}' -b cookie.txt
 * curl 0.0.0.0:8080/component/2 -X PUT -H "Content-Type: application/json" -d '{"id":2,"title":"a","type":"MB","description":"b","cost":111,"image":null}' -b cookie.txt
 * curl 0.0.0.0:8080/component/2 -X DELETE -b cookie.txt
 * curl 0.0.0.0:8080/component/type/0
 * curl 0.0.0.0:8080/register -X POST -H "Content-Type: application/json" -d '{"name":"test","password":"pass"}'
 * curl 0.0.0.0:8080/login -X POST -F 'name=user' -F 'password=user' -c cookie.txt
 * curl 0.0.0.0:8080/login -X POST -F 'name=admin' -F 'password=admin' -c cookie.txt
 * curl 0.0.0.0:8080/logout -X POST -b cookie.txt -c cookie.txt
 * curl 0.0.0.0:8080/selected -b cookie.txt
 * curl 0.0.0.0:8080/select/1 -X POST -b cookie.txt
 * curl 0.0.0.0:8080/clearSelected -X POST -b cookie.txt
 * curl 0.0.0.0:8080/history -b cookie.txt
 * curl 0.0.0.0:8080/history -X DELETE -b cookie.txt
 * curl 0.0.0.0:8080/file/a.jpg --data-binary "@a.jpg" -X POST -b cookie.txt
 * curl 0.0.0.0:8080/file/a.jpg -X DELETE -b cookie.txt
 * curl 0.0.0.0:8080/order -X POST -H "Content-Type: application/json" -d '{"firstName":"fn","lastName":"ln","phone":1234567890,"address":"address"}' -b cookie.txt
 * curl 0.0.0.0:8080/name -b cookie.txt
 * curl 0.0.0.0:8080/user -X POST -H "Content-Type: application/json" -d '{"id":3,"name":"test","role":"USER","password":"test","firstName":null,"lastName":null,"phone":null,"address":null,"selection":null}' -b cookie.txt
 * curl 0.0.0.0:8080/user -b cookie.txt
 * curl 0.0.0.0:8080/user/3 -b cookie.txt
 * curl 0.0.0.0:8080/user/3 -X PUT -H "Content-Type: application/json" -d '{"id":3,"name":"test","role":"USER","password":"pass","firstName":null,"lastName":null,"phone":null,"address":null,"selection":null}' -b cookie.txt
 * curl 0.0.0.0:8080/user/3 -X DELETE -b cookie.txt
 * curl 0.0.0.0:8080/authorizedU
 * curl 0.0.0.0:8080/authorizedA
 * curl 0.0.0.0:8080/session -b cookie.txt
 * curl 0.0.0.0:8080/session/a -b cookie.txt
 * curl 0.0.0.0:8080/session -X POST -H "Content-Type: application/json" -d '{"id":"a","value":"b"}' -b cookie.txt
 * curl 0.0.0.0:8080/session/a -X PUT -H "Content-Type: application/json" -d '{"id":"a","value":"bb"}' -b cookie.txt
 * curl 0.0.0.0:8080/session/a -X DELETE -b cookie.txt
 */
fun Application.configureRouting() = routing {
    componentRouting()
    userRouting()
    sessionRouting()
    staticRouting()
}

private fun Routing.staticRouting() = static {
    resource("/", "/static/index.html")
    static("/") { resources("static") }
    static("/assets") { resources("static/assets") }
    static("/res_back") { files("/res_back") }
}

private fun Routing.componentRouting() = route("/component") {
    authAdmin { post { componentService.add() } }
    get { componentService.getAll() }
    route(idParam) {
        get { componentService.getById() }
        authAdmin {
            put { componentService.update() }
            delete { componentService.delete() }
        }
    }
    get("/type/{$TYPE}") { componentService.getByType() }
}

private fun Routing.userRouting() {
    authenticate(SESSION_USER, SESSION_ADMIN, optional = true) {
        post("/register") { userService.register() }
        post("/logout") { userService.logout() }
        get("/name") { userService.name() }
    }
    authenticate(AUTH_FORM) { post("/login") { userService.login() } }
    authUser {
        get("/authorizedU") { userService.authorized() }
        post("/select/$idParam") { userService.select() }
        get("/selected") { userService.selected() }
        post("/clearSelected") { userService.clearSelected() }
        get("/history") { userService.selectionHistory() }
        delete("/history") { userService.clearHistory() } // TODO: add possibility to add and delete component images and store them in separate mutable folder and display images in admin page
        post("/order") { userService.order() }
    }
    authAdmin {
        get("/authorizedA") { userService.authorized() }
        route("/user") {
            post { userService.add() }
            get { userService.getAll() }
            route(idParam) {
                get { userService.getById() }
                put { userService.update() }
                delete { userService.delete() }
            }
        }
        route("/file") {
            get { userService.getFileNames() }
            "/{file}".let {
                post(it) { userService.uploadFile() }
                delete(it) { userService.removeFile() }
            }
        }
    }
}

private fun Routing.sessionRouting() = authAdmin { route("/session") {
    get { sessionService.getAll() }
    post { sessionService.add() }
    route(idParam) {
        get { sessionService.getById() }
        put { sessionService.update() }
        delete { sessionService.delete() }
    }
} }
