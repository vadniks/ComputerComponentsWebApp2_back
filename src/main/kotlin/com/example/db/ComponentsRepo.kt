package com.example.db

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Component
import com.example.db.models.Components
import com.example.db.models.Components.cost
import com.example.db.models.Components.description
import com.example.db.models.Components.id
import com.example.db.models.Components.image
import com.example.db.models.Components.title
import com.example.db.models.Components.type
import com.example.db.models.Type
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ComponentsRepo : AbsRepo<Component, Components>(Components, id) {

    override fun resultRowToEntity(row: ResultRow): Component =
        Component(row[id], row[title], row[type], row[description], row[cost], row[image])

    suspend fun getAll(type: Type): List<Component> = getAllBy(Components.type eq type)

    override suspend fun add(entity: Component): Boolean = performAdd {
        if (entity.id != null) it[id] = entity.id
        it[title] = entity.title
        it[type] = entity.type
        it[description] = entity.description
        it[cost] = entity.cost
        it[image] = entity.image
    }

    override suspend fun exactPresents(entity: Component): Boolean = dbQuery { !Components.select(
        (if (entity.id != null) id eq entity.id else Op.TRUE) and
        (title eq entity.title) and
        (type eq entity.type) and
        (description eq entity.description) and
        (cost eq entity.cost) and
        (image eq entity.image)
    ).empty() }

    suspend fun getBy(title: String): Component? = getBy(Components.title eq title)

    override suspend fun update(id: Int, entity: Component): Boolean
    = performUpdate(id) {
        it[title] = entity.title
        it[type] = entity.type
        it[description] = entity.description
        it[cost] = entity.cost
        it[image] = entity.image
    }

    init { runBlocking {
        addIfNotExists(Component("test", Type.CPU, "test+", 100, null))
    } }
}
