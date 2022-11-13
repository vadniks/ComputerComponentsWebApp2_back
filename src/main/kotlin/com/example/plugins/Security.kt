package com.example.plugins

import com.example.db.models.NAME
import com.example.db.models.PASSWORD
import com.example.db.models.Role
import com.example.db.repo.SessionStorageDatabase
import com.example.db.repo.UsersRepo
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*

const val SESSION_USER = "session_user"
const val SESSION_ADMIN = "session_admin"
const val AUTH_FORM = "form"
const val COOKIE = "cookie"

data class UserIdPrincipal(val id: Int) : Principal

suspend fun ApplicationCall.respondForbidden() = respond(HttpStatusCode.Forbidden)
suspend fun ApplicationCall.respondUnauthorized() = respond(HttpStatusCode.Unauthorized)

private fun sessionConfig(role: Role): SessionAuthenticationProvider.Config<UserIdPrincipal>.() -> Unit = {
    validate { if (UsersRepo.checkRole(it.id, role)) it else null }
    challenge { call.respondForbidden() }
}

private val auth: suspend ApplicationCall.(UserPasswordCredential) -> UserIdPrincipal? = {
    val id = UsersRepo.checkCredentials(it.name, it.password)
    if (id == null || id < 1) null
    else UserIdPrincipal(id)
}

fun Application.configureSecurity() {
    install(Authentication) {
        session(SESSION_USER, sessionConfig(Role.USER))
        session(SESSION_ADMIN, sessionConfig(Role.ADMIN))
        form(AUTH_FORM) {
            userParamName = NAME
            passwordParamName = PASSWORD
            challenge { call.respondUnauthorized() }
            validate(auth)
        }
    }
    install(Sessions) { cookie<UserIdPrincipal>(COOKIE, SessionStorageDatabase()) { transform(
        SessionTransportTransformerEncrypt(
            hex("00112233445566778899aabbccddeeff"),
            hex("6819b57a326945c1968f45236589")
        )
    ) } }
}
