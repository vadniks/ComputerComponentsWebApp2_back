package com.example.db.repo

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Users
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.logging.Logger

abstract class AbsRepo<E, T: Table, C>(private val table: T, private val idColumn: Column<C>) {

    protected abstract fun resultRowToEntity(row: ResultRow): E

    private fun Query.mapToEntity(): List<E> = map(::resultRowToEntity)

    suspend fun getAll(): List<E> = dbQuery { table.selectAll().mapToEntity() }

    suspend fun getAllBy(selection: Op<Boolean>): List<E> = dbQuery { table.select(selection).mapToEntity() }

    private fun InsertStatement<Number>.oneRowAffected(): Boolean = insertedCount == 1

    protected abstract fun setValues(it: UpdateBuilder<Int>, entity: E)

    private suspend fun add(entity: E): Boolean
    = dbQuery { table.insert { setValues(it, entity) }.oneRowAffected() }

    protected abstract suspend fun exactPresents(entity: E): Boolean

    suspend fun addIfNotExists(entity: E) = if (!exactPresents(entity)) add(entity) else false

    protected suspend fun getBy(selection: Op<Boolean>): E? = dbQuery { table
        .select(selection)
        .mapLazy(::resultRowToEntity)
        .singleOrNull() }

    protected suspend fun <T> getSingle(selection: Op<Boolean>, which: Column<T>): T?
    = dbQuery { table.select(selection).mapLazy { it[which] }.singleOrNull() }

    suspend fun getBy(id: C): E? = getBy(idColumn eq id)

    suspend fun update(id: C, entity: E): Boolean
    = dbQuery { table.update({ idColumn eq id }) { setValues(it, entity) } == 1 }

    protected suspend fun <T> updateSingle(selection: Op<Boolean>, which: Column<T>, what: T): Boolean
    = dbQuery { Users.update({ selection }) { it[which] = what } == 1 }

    suspend fun delete(id: C): Boolean = dbQuery { table.deleteWhere { idColumn eq id } == 1 }

    init { runBlocking { dbQuery { it.exec("select count(*) from " + table.tableName) { resultSet ->
        assert(resultSet.next())
        if (resultSet.getInt(1) == 0) addTests(*testEntities())
    } } } }

    @TestOnly private fun addTests(vararg entities: E) = runBlocking { entities.forEach { addIfNotExists(it) } }

    @TestOnly abstract fun testEntities(): Array<E>
}
