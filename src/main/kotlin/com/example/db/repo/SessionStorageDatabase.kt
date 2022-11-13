package com.example.db.repo

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Sessions
import com.example.unitStub
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SessionStorageDatabase : SessionStorage {

    override suspend fun invalidate(id: String): Unit = dbQuery { Sessions.deleteWhere { Sessions.id eq id } }.unitStub

    override suspend fun read(id: String): String = dbQuery { Sessions
        .select(Sessions.id eq id)
        .mapLazy { it[Sessions.value] }
        .singleOrNull() ?: throw NoSuchElementException()
    }

    override suspend fun write(id: String, value: String): Unit = dbQuery {
        Sessions.update({ Sessions.id eq id }) { it[Sessions.value] = value }.takeIf { it == 1 }
            ?: Sessions.insert { it[Sessions.id] = id; it[Sessions.value] = value }.takeIf { it.insertedCount == 1 }
            ?: throw IllegalStateException()
    }.unitStub
}
