package com.example.service

import com.example.db.models.Selection
import com.example.db.models.set
import com.example.db.models.toSelection
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
        val id = call.getIdParameter()
        if (id == null) {
            call.respondUserError()
            return@doIfTokenIsNotNull
        }

        val component = ComponentService.getById(id)
        if (component == null) {
            call.respondUserError()
            return@doIfTokenIsNotNull
        }

        setSelection(it, (getSelection(it)?.toSelection() ?: Selection()).apply { this[component.type] = id }.toString())
        call.respondOk()
    }

    suspend fun selected(call: ApplicationCall) = call.respondIfTokenIsNotNull { getSelection(it) }

    suspend fun clearSelected(call: ApplicationCall) = call.doIfTokenIsNotNull {
        setSelection(it, null)
        call.respondOk()
    }
}
