package com.example.service

import io.ktor.server.application.*

fun <T : AbsService> ApplicationCall.service(service: T) = service.also { it.call = this }

abstract class AbsService { lateinit var call: ApplicationCall }
