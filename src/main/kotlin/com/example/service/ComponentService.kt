package com.example.service

import com.example.db.repo.ComponentsRepo
import com.example.plugins.getId
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

object ComponentService {

    suspend fun add(call: ApplicationCall) = ComponentsRepo.addIfNotExists(call.receive())

    suspend fun getAll(call: ApplicationCall) = call.respond(ComponentsRepo.getAll())

    suspend fun getById(call: ApplicationCall) = call.respondNullable(ComponentsRepo.getBy(getId(call)))

    suspend fun update(call: ApplicationCall) = ComponentsRepo.update(getId(call), call.receive())

    suspend fun delete(call: ApplicationCall) = ComponentsRepo.delete(getId(call))
}
