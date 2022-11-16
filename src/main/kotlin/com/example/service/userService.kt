package com.example.service

import com.example.db.models.*
import com.example.db.repo.UsersRepo
import com.example.db.repo.UsersRepo.getSelection
import com.example.db.repo.UsersRepo.setSelection
import com.example.plugins.*
import com.example.plugins.UserIdPrincipal
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import java.security.MessageDigest

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

    fun hash(value: String) = hex(MessageDigest.getInstance("SHA-256").digest(value.encodeToByteArray()))

    suspend fun register() = call.receive<UserCredentials>().run {
        if (call.principal<UserIdPrincipal>() != null) { call.respondUserError(); return }
        if (UsersRepo.nameExists(this.name)) call.respondUserError()
        else call.respondOkIfTrue(UsersRepo.addIfNotExists(User(
            name = this.name,
            password = hash(this.password),
            role = Role.USER
        )))
    }

    suspend fun getAll() = call.respond(UsersRepo.getAll())
    suspend fun getById() = call.respondIfIdParameterIsNotNull { UsersRepo.getBy(it) }
    suspend fun add() = call.respondOkIfTrue(UsersRepo.addIfNotExists(call.receive()))
    suspend fun update() = call.respondOkIfTrueWithIdParameter { UsersRepo.update(it, call.receive()) }
    suspend fun delete() = call.respondOkIfTrueWithIdParameter { UsersRepo.delete(it) }
}
