package com.example.service

import com.example.db.models.Selection
import com.example.db.models.set
import com.example.db.models.toSelection
import com.example.db.repo.UsersRepo.getSelection
import com.example.db.repo.UsersRepo.setSelection
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
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

    suspend fun select(call: ApplicationCall) = call.doIfUserIdFound {
        val id = call.getIdParameter()
        if (id == null) {
            call.respondUserError()
            return@doIfUserIdFound
        }

        val component = ComponentService.getById(id)
        if (component == null) {
            call.respondUserError()
            return@doIfUserIdFound
        }

        setSelection(it, (getSelection(it)?.toSelection() ?: Selection()).apply { this[component.type] = id }.toString())
        call.respondOk()
    }

    suspend fun selected(call: ApplicationCall) = call.doIfUserIdFound { call.respondNullable(getSelection(it)) }

    suspend fun clearSelected(call: ApplicationCall) = call.doIfUserIdFound {
        setSelection(it, null)
        call.respondOk()
    }
}
