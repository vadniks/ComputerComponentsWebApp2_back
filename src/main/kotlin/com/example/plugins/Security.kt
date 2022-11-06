package com.example.plugins

import com.example.db.models.NAME
import com.example.db.models.PASSWORD
import com.example.db.models.Role
import com.example.db.repo.UsersRepo
import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.MessageDigest

private const val SESSION_USER = "session_user"
private const val SESSION_ADMIN = "session_admin"
private const val COOKIE = "cookie"

private data class UserTokenPrincipal(val token: String) : Principal

private class AuthenticationException : RuntimeException()

private fun sessionConfig(role: Role): SessionAuthenticationProvider.Config<UserTokenPrincipal>.() -> Unit = {
    validate { if (UsersRepo.checkRole(it.token, role)) it else null }
    challenge { throw AuthenticationException() }
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
        form {
            userParamName = NAME
            passwordParamName = PASSWORD
            challenge { throw AuthenticationException() }
            validate(auth)
        }
    }
    install(Sessions) {
        cookie<UserTokenPrincipal>(COOKIE) {}
    }
}

fun Application.OldconfigureSecurity() {

    authentication {
        val myRealm = "MyRealm"
        val usersInMyRealmToHA1: Map<String, ByteArray> = mapOf(
            // pass="test", HA1=MD5("test:MyRealm:pass")="fb12475e62dedc5c2744d98eb73b8877"
            "test" to hex("fb12475e62dedc5c2744d98eb73b8877")
        )

        digest("myDigestAuth") {
            digestProvider { userName, realm ->
                usersInMyRealmToHA1[userName]
            }
        }
    }
    data class MySession(val count: Int = 0)
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    routing {
        authenticate("myDigestAuth") {
            get("/protected/route/digest") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}
