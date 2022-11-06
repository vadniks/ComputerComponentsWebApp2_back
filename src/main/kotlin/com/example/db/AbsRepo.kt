package com.example.db

import com.example.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

abstract class AbsRepo<E, T: Table>(protected val table: T, protected val idColumn: Column<Int>) {

    protected abstract fun resultRowToEntity(row: ResultRow): E

    private fun Query.mapToEntity(): List<E> = map(::resultRowToEntity)

    suspend fun getAll(): List<E> = dbQuery { table.selectAll().mapToEntity() }

    suspend fun getAllBy(selection: Op<Boolean>): List<E> = dbQuery { table.select(selection).mapToEntity() }

    private fun InsertStatement<Number>.oneRowAffected(): Boolean = insertedCount == 1

    protected suspend fun performAdd(body: T.(InsertStatement<Number>) -> Unit): Boolean
    = dbQuery { table.insert(body).oneRowAffected() }

    protected abstract suspend fun add(entity: E): Boolean

    protected abstract suspend fun exactPresents(entity: E): Boolean

    suspend fun addIfNotExists(entity: E) = if (!exactPresents(entity)) add(entity) else false

    protected suspend fun getBy(selection: Op<Boolean>): E? = dbQuery { table
        .select(selection)
        .mapLazy(::resultRowToEntity)
        .singleOrNull() }

    suspend fun getBy(id: Int): E? = getBy(idColumn eq id)

    protected suspend fun performUpdate(id: Int, body: T.(UpdateStatement) -> Unit): Boolean
    = dbQuery { table.update({ idColumn eq id }, body = body) == 1 }

    abstract suspend fun update(id: Int, entity: E): Boolean

    suspend fun delete(id: Int): Boolean = dbQuery { table.deleteWhere { idColumn eq id } == 1 }
}
