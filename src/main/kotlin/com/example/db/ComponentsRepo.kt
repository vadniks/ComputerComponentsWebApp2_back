package com.example.db

import com.example.db.DatabaseFactory.dbQuery
import com.example.models.Component
import com.example.models.Components
import com.example.models.Components.cost
import com.example.models.Components.description
import com.example.models.Components.id
import com.example.models.Components.image
import com.example.models.Components.title
import com.example.models.Components.type
import com.example.models.Type
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ComponentsRepo private constructor() {

    private fun resultRowToComponent(row: ResultRow) =
        Component(row[id], row[title], row[type], row[description], row[cost], row[image])

    suspend fun getComponents(): List<Component> =
        dbQuery { Components.selectAll().map(::resultRowToComponent) }

    suspend fun getComponents(type: Type): List<Component> =
        dbQuery { Components.select(Components.type eq type).map(::resultRowToComponent) }

    private suspend fun addComponent(component: Component): Boolean = dbQuery { Components.insert {
        if (component.id != null) it[id] = component.id
        it[title] = component.title
        it[type] = component.type
        it[description] = component.description
        it[cost] = component.cost
        it[image] = component.image
    }.insertedCount == 1 }

    suspend fun addComponentIfNotExists(component: Component) =
        if (!exactPresents(component)) addComponent(component) else false

    private suspend fun exactPresents(component: Component): Boolean = dbQuery { !Components.select(
        (id eq component.id!!) and
        (title eq component.title) and
        (type eq component.type) and
        (description eq component.description) and
        (cost eq component.cost) and
        (image eq component.image)
    ).empty() }

    private suspend fun getComponent(selection: Op<Boolean>): Component? = dbQuery { Components
        .select(selection)
        .mapLazy(::resultRowToComponent)
        .takeUnless { it.empty() }?.single() }

    suspend fun getComponent(id: Int) = getComponent(Components.id eq id)

    suspend fun getComponent(title: String) = getComponent(Components.title eq title)

    suspend fun updateComponent(component: Component): Unit =
}
