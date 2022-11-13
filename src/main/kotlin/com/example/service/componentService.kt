package com.example.service

import com.example.db.repo.ComponentsRepo
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

val ApplicationCall.componentService get() = ComponentService.also { it.call = this }

object ComponentService : AbsService() {
    suspend fun add() = call.respondOkIfTrue(ComponentsRepo.addIfNotExists(call.receive()))
    suspend fun getAll() = call.respond(ComponentsRepo.getAll())
    suspend fun getById() = call.respondIfIdParameterIsNotNull { getById(it) }
    suspend fun getById(id: Int) = ComponentsRepo.getBy(id)
    suspend fun update() = call.respondOkIfTrueWithIdParameter { ComponentsRepo.update(it, call.receive()) }
    suspend fun delete() = call.respondOkIfTrueWithIdParameter { ComponentsRepo.delete(it) }
}
