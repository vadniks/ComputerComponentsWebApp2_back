package com.example.service

import com.example.db.repo.ComponentsRepo
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

object ComponentService {

    suspend fun add(call: ApplicationCall) = call.respondOkITrue(ComponentsRepo.addIfNotExists(call.receive()))

    suspend fun getAll(call: ApplicationCall) = call.respond(ComponentsRepo.getAll())

    suspend fun getById(call: ApplicationCall) = call.respondIfIdParameterIsNotNull { getById(it) }

    suspend fun getById(id: Int) = ComponentsRepo.getBy(id)

    suspend fun update(call: ApplicationCall) =
        call.doIfIdParameterIsNotNull { call.respondOkITrue(ComponentsRepo.update(it, call.receive())) }

    suspend fun delete(call: ApplicationCall) =
        call.doIfIdParameterIsNotNull { call.respondOkITrue(ComponentsRepo.delete(it)) }
}
