package com.example.db.repo

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Sessions
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.mapLazy
import org.jetbrains.exposed.sql.select

class SessionStorageDatabase : SessionStorage { // TODO: not working

    override suspend fun invalidate(id: String) { dbQuery {
        if (Sessions.deleteWhere { Sessions.id eq id } != 1)
            throw NoSuchElementException()
    } }

    override suspend fun read(id: String): String = dbQuery { Sessions
        .select(Sessions.id eq id)
        .mapLazy { it[Sessions.value] }
        .singleOrNull() ?: throw NoSuchElementException()
    }

    override suspend fun write(id: String, value: String) = dbQuery {
        if (Sessions.insert { it[Sessions.id] = id; it[Sessions.value] = value }.insertedCount != 1)
            throw RuntimeException()
    }
}
