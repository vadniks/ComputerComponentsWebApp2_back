package com.example.db

import com.example.models.Components
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    init { transaction(Database.connect(
        "jdbc:postgresql://db:5432/db",
        "org.postgresql.Driver",
        "postgres",
        "postgres"
    )) { SchemaUtils.create(Components) } }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }
}
