package com.example.service

import com.example.db.models.Component
import com.example.db.models.TYPE
import com.example.db.models.toType
import com.example.db.repo.ComponentsRepo
import com.example.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*

object ComponentService : AbsService() {
    suspend fun add() = call.respondOkIfTrue(ComponentsRepo.addIfNotExists(call.receive()))
    suspend fun getAll() = call.respond(ComponentsRepo.getAll())
    suspend fun getById() = call.respondIfIdParameterIsNotNull { getById(it) }
    suspend fun getById(id: Int) = ComponentsRepo.getBy(id)
    suspend fun update() = call.respondOkIfTrueWithIdParameter { ComponentsRepo.update(it, call.receive()) }
    suspend fun delete() = call.respondOkIfTrueWithIdParameter { ComponentsRepo.delete(it) }

    suspend fun getByType() = call.parameters[TYPE]?.toIntOrNull().apply {
        if (this == null) call.respondUserError()
        else getByType(this).apply { call.respondNullable(this) }
    }

    private suspend fun getByType(type: Int): List<Component>?
    = type.toType().run { if (this != null) ComponentsRepo.getAll(this) else null }
}
