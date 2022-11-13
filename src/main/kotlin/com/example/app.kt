@file:JvmName("appKt")

package com.example

import com.example.db.DatabaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

@Suppress("unused") val Any.unitStub get() = Unit

fun main() = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
    configureSecurity()
    configureHTTP()
    configureRouting()
    install(ContentNegotiation) { json() }
    DatabaseFactory
}.start(wait = true).unitStub
