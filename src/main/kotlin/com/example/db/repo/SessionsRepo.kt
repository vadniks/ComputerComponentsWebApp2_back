package com.example.db.repo

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Session
import com.example.db.models.Sessions
import com.example.db.models.Sessions.id
import com.example.db.models.Sessions.value
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object SessionsRepo : AbsRepo<Session, Sessions, String>(Sessions, id) {

    override fun resultRowToEntity(row: ResultRow) = Session(row[id], row[value])

    override fun testEntities(): Array<Session> = emptyArray()

    override suspend fun exactPresents(entity: Session): Boolean
    = dbQuery { !Sessions.select((id eq entity.id) and (value eq entity.value)).empty() }

    override fun setValues(it: UpdateBuilder<Int>, entity: Session) {
        it[id] = entity.id
        it[value] = entity.value
    }

    suspend fun updateOrCreate(session: Session) = dbQuery {
        Sessions.update({ id eq session.id }) { it[value] = session.value }.takeIf { it == 1 }
            ?: Sessions.insert { it[id] = session.id; it[value] = session.value }.takeIf { it.insertedCount == 1 }
            ?: throw IllegalStateException()
    }
}
