package com.example.service

import com.example.plugins.UserTokenPrincipal
import com.example.plugins.respondOk
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*

object UserService {

    suspend fun login(call: ApplicationCall) {
        call.sessions.set(call.principal<UserTokenPrincipal>()!!)
        respondOk(call)
    }

    suspend fun logout(call: ApplicationCall) {
        call.sessions.clear<UserTokenPrincipal>()
        respondOk(call)
    }

    suspend fun select() {

    }
}
