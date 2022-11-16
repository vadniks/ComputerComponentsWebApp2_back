package com.example.db

import com.example.db.models.Components
import com.example.db.models.Sessions
import com.example.db.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    init { transaction(Database.connect(
        "jdbc:postgresql://localhost:5432/db",
        "org.postgresql.Driver",
        "postgres",
        "postgres"
    )) { SchemaUtils.create(Components, Users, Sessions) } }

    suspend fun <T> dbQuery(block: suspend (Transaction) -> T): T = newSuspendedTransaction { block(this) }
}
