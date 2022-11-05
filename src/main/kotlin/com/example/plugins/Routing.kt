package com.example.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {


    routing {
//        get("/") {
//            call.respondFile(File("/static/index.html"))
//        }

        static {
            resource("/", "/static/index.html")
            static("/") {
                resources("static")
            }
        }
    }
}
