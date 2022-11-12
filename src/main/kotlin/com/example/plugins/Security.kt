package com.example.plugins

import com.example.db.models.NAME
import com.example.db.models.PASSWORD
import com.example.db.models.Role
import com.example.db.repo.UsersRepo
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.MessageDigest

const val SESSION_USER = "session_user"
const val SESSION_ADMIN = "session_admin"
const val FORM = "form"
const val COOKIE = "cookie"

data class UserTokenPrincipal(val token: String) : Principal

suspend fun ApplicationCall.respondForbidden() = respond(HttpStatusCode.Forbidden)
suspend fun ApplicationCall.respondUnauthorized() = respond(HttpStatusCode.Unauthorized)

private fun sessionConfig(role: Role): SessionAuthenticationProvider.Config<UserTokenPrincipal>.() -> Unit = {
    validate { if (UsersRepo.checkRole(it.token, role)) it else null }
    challenge { call.respondForbidden() }
}

private val auth: suspend ApplicationCall.(UserPasswordCredential) -> UserTokenPrincipal? = {
    val id = UsersRepo.checkCredentials(it.name, it.password)
    if (id == null || id < 1) null
else {
    val token = MessageDigest.getInstance("SHA-256")
        .digest((it.name + it.password).encodeToByteArray()).encodeBase64()
    UsersRepo.setToken(id, token)
    UserTokenPrincipal(token)
} }

fun Application.configureSecurity() {
    install(Authentication) {
        session(SESSION_USER, sessionConfig(Role.USER))
        session(SESSION_ADMIN, sessionConfig(Role.ADMIN))
        form(FORM) {
            userParamName = NAME
            passwordParamName = PASSWORD
            challenge { call.respondUnauthorized() }
            validate(auth)
        }
    }
    install(Sessions) {
        cookie<UserTokenPrincipal>(COOKIE) {}
    }
}
