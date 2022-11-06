package com.example.db

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import io.ktor.server.sessions.*
import kotlinx.coroutines.runBlocking

class SessionStorageRedis : SessionStorage {
    private val redisClient = runBlocking { newClient(Endpoint.from("localhost:6379")) }

    override suspend fun read(id: String): String = redisClient.get(id) ?: throw NoSuchElementException()

    override suspend fun write(id: String, value: String) { redisClient.set(id, value) }

    override suspend fun invalidate(id: String) { redisClient.del(id) }
}
