package com.example.service

import com.example.db.repo.ComponentsRepo
import com.example.plugins.getIdParameter
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

object ComponentService {

    suspend fun add(call: ApplicationCall) = ComponentsRepo.addIfNotExists(call.receive())

    suspend fun getAll(call: ApplicationCall) = call.respond(ComponentsRepo.getAll())

    suspend fun getById(call: ApplicationCall) = call.respondNullable(ComponentsRepo.getBy(call.getIdParameter()))

    suspend fun update(call: ApplicationCall) = ComponentsRepo.update(call.getIdParameter(), call.receive())

    suspend fun delete(call: ApplicationCall) = ComponentsRepo.delete(call.getIdParameter())
}
