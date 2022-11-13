package com.example.db.repo

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Sessions
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SessionStorageDatabase : SessionStorage { // TODO: not working

    override suspend fun invalidate(id: String) { dbQuery { Sessions.deleteWhere { Sessions.id eq id } } }

    override suspend fun read(id: String): String = dbQuery { Sessions
        .select(Sessions.id eq id)
        .mapLazy { it[Sessions.value] }
        .singleOrNull() ?: throw NoSuchElementException()
    }

    override suspend fun write(id: String, value: String) = dbQuery {
//        if (Sessions.update({ Sessions.id eq id }) { it[Sessions.value] = value } == 0)
//            if (Sessions.insert { it[Sessions.id] = id; it[Sessions.value] = value }.insertedCount != 1)
//                throw RuntimeException()
//        else throw NoSuchElementException()
        Sessions.update({ Sessions.id eq id }) { it[Sessions.value] = value }.takeIf { it == 1 }
            ?: Sessions.insert { it[Sessions.id] = id;it[Sessions.value] = value }.takeIf { it.insertedCount == 1 }
            ?: throw IllegalStateException()
    }.run {}
}
