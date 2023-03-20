package com.example.service

import com.example.db.models.Component
import com.example.db.models.TYPE
import com.example.db.models.toType
import com.example.db.repo.ComponentsRepo
import com.example.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

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

    private suspend inline fun doIfFileSupplied(crossinline action: suspend (String) -> Unit) = call.parameters["file"].let {
        if (it == null) {
            call.respondUserError()
            return@let
        }
        action(it)
    }

    private fun makeImageFile(name: String) = File("/res_back/$name")

    suspend fun uploadFile() = doIfFileSupplied {
        call.receiveChannel().copyAndClose(makeImageFile(it).writeChannel())
        call.respondOk()
    }

    suspend fun removeFile() = doIfFileSupplied { call.respondOkIfTrue(makeImageFile(it).delete()) }

    suspend fun getFileNames() {
        var str = ""
        File("/res_back").listFiles()?.forEach {
            str += (if (str.isNotEmpty()) ":" else "") + it.name
        }
        call.respondNullable(str.takeIf { str.isNotEmpty() })
    }
}
