package com.example.service

import com.example.db.repo.UsersRepo.getSelection
import com.example.db.repo.UsersRepo.setSelection
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*

object UserService {

    suspend fun login(call: ApplicationCall) {
        call.sessions.set(call.principal<UserTokenPrincipal>()!!)
        call.respondOk()
    }

    suspend fun logout(call: ApplicationCall) {
        call.sessions.clear<UserTokenPrincipal>()
        call.respondOk()
    }

    suspend fun select(call: ApplicationCall) = call.doIfTokenIsNotNull {
        val selection = getSelection(it) ?: ""
        selection + call.getIdParameter() + ','
        setSelection(it, selection)
    }

    suspend fun selected(call: ApplicationCall) = call.respondIfTokenIsNotNull { getSelection(it) }

    suspend fun clearSelected(call: ApplicationCall) =
        call.doIfTokenIsNotNull { setSelection(it, null) }
}
