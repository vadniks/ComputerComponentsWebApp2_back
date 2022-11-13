package com.example.service

import com.example.db.models.Selection
import com.example.db.models.set
import com.example.db.models.toSelection
import com.example.db.repo.UsersRepo.getSelection
import com.example.db.repo.UsersRepo.setSelection
import com.example.plugins.*
import com.example.plugins.UserIdPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

object UserService {

    suspend fun login(call: ApplicationCall) {
        call.sessions.set(call.principal<UserIdPrincipal>()!!)
        call.respondOk()
    }

    suspend fun logout(call: ApplicationCall) {
        call.sessions.clear<UserIdPrincipal>()
        call.respondOk()
    }

    suspend fun select(call: ApplicationCall) = call.doIfUserIdFound { userId ->
        val componentId = call.getIdParameter()
        if (componentId == null) {
            call.respondUserError()
            return@doIfUserIdFound
        }

        val component = ComponentService.getById(componentId)
        if (component == null) {
            call.respondUserError()
            return@doIfUserIdFound
        }

        setSelection(
            userId,
            (getSelection(userId)?.toSelection() ?: Selection()).apply { this[component.type] = componentId }.toString()
        )
        call.respondOk()
    }

    suspend fun selected(call: ApplicationCall) = call.doIfUserIdFound { call.respondNullable(getSelection(it)) }

    suspend fun clearSelected(call: ApplicationCall) = call.doIfUserIdFound {
        setSelection(it, null)
        call.respondOk()
    }
}
