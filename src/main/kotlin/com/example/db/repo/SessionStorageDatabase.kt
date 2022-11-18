package com.example.db.repo

import com.example.db.models.Session
import com.example.service.SessionService
import com.example.unitStub
import io.ktor.server.sessions.*

// docker exec -it cursov_db psql db postgres
class SessionStorageDatabase : SessionStorage {

    override suspend fun invalidate(id: String) = SessionService.delete(id).unitStub

    override suspend fun read(id: String): String = SessionService.getBy(id)?.value ?: throw NoSuchElementException()

    override suspend fun write(id: String, value: String) = SessionService.updateOrCreate(Session(id, value)).unitStub
}
