package com.example.service

import com.example.db.models.ID
import com.example.db.models.Session
import com.example.db.repo.SessionsRepo
import com.example.plugins.respondOkIfTrue
import com.example.plugins.respondUserError
import io.ktor.server.request.*
import io.ktor.server.response.*

object SessionService : AbsService() {
    private val idParam get() = call.parameters[ID]

    suspend fun add() = call.respondOkIfTrue(SessionsRepo.addIfNotExists(call.receive()))
    suspend fun getAll() = call.respond(SessionsRepo.getAll())
    suspend fun getBy(id: String) = SessionsRepo.getBy(id)
    suspend fun getById() = idParam.apply { respondIfNotNullOrRespondUserError { getBy(it) } }
    suspend fun updateOrCreate(session: Session) = SessionsRepo.updateOrCreate(session)

    suspend fun update() = idParam.apply { respondIfNotNullOrRespondUserError {
        call.respondOkIfTrue(SessionsRepo.update(it, call.receive()))
    } }

    suspend fun delete(id: String) = SessionsRepo.delete(id)

    suspend fun delete() = idParam.apply { respondIfNotNullOrRespondUserError {
        call.respondOkIfTrue(delete(it))
    } }

    private suspend inline fun <T> T?.respondIfNotNullOrRespondUserError(crossinline action: suspend (T) -> Any?)
    = this.run { if (this != null) call.respondNullable(action(this)) else call.respondUserError() }
}
