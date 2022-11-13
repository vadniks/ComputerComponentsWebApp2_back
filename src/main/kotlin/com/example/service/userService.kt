package com.example.service

import com.example.db.models.Selection
import com.example.db.models.UserDetails
import com.example.db.models.set
import com.example.db.models.toSelection
import com.example.db.repo.UsersRepo
import com.example.db.repo.UsersRepo.getSelection
import com.example.db.repo.UsersRepo.setSelection
import com.example.plugins.*
import com.example.plugins.UserIdPrincipal
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

object UserService : AbsService() {

    suspend fun login() {
        call.sessions.set(call.principal<UserIdPrincipal>()!!)
        call.respondOk()
    }

    suspend fun logout() {
        call.sessions.clear<UserIdPrincipal>()
        call.respondOk()
    }

    suspend fun select() = call.doIfUserIdFound { userId ->
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

    suspend fun selected() = call.doIfUserIdFound { call.respondNullable(getSelection(it)) }

    suspend fun clearSelected() = call.doIfUserIdFound {
        setSelection(it, null)
        call.respondOk()
    }

    suspend fun order() = call.doIfUserIdFound {
        val details = call.receive<UserDetails>()

        val user = UsersRepo.getBy(it)
        if (user == null) { call.respondUserError(); return@doIfUserIdFound }

        call.respondOkIfTrue(UsersRepo.update(it, user.copy(
            firstName = details.firstName,
            lastName = details.lastName,
            phone = details.phone,
            address = details.address
        )))
    }

    suspend fun getAll() = call.respond(UsersRepo.getAll())
    suspend fun getById() = call.respondIfIdParameterIsNotNull { UsersRepo.getBy(it) }
    suspend fun add() = call.respondOkIfTrue(UsersRepo.addIfNotExists(call.receive()))
    suspend fun update() = call.respondOkIfTrueWithIdParameter { UsersRepo.update(it, call.receive()) }
    suspend fun delete() = call.respondOkIfTrueWithIdParameter { UsersRepo.delete(it) }
}
