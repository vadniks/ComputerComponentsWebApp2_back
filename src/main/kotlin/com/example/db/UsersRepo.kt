package com.example.db

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.User
import com.example.db.models.Users
import com.example.db.models.Users.address
import com.example.db.models.Users.firstName
import com.example.db.models.Users.id
import com.example.db.models.Users.lastName
import com.example.db.models.Users.name
import com.example.db.models.Users.password
import com.example.db.models.Users.phone
import com.example.db.models.Users.role
import com.example.db.models.Users.selection
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object UsersRepo : AbsRepo<User, Users>(Users, Users.id) {

    override fun resultRowToEntity(row: ResultRow): User = User(
        row[id],       row[name],      row[role],
        row[password], row[firstName], row[lastName],
        row[phone],    row[address],   row[selection]
    )

    @Suppress("DuplicatedCode")
    override fun setValues(it: UpdateBuilder<Int>, entity: User) {
        if (entity.id != null) it[id] = entity.id
        it[name] = entity.name
        it[role] = entity.role
        it[password] = entity.password
        it[firstName] = entity.firstName
        it[lastName] = entity.lastName
        it[phone] = entity.phone
        it[address] = entity.address
        it[selection] = entity.selection
    }

    override suspend fun exactPresents(entity: User): Boolean = dbQuery { !Users.select(
        (if (entity.id != null) id eq entity.id else Op.TRUE) and
        (name eq entity.name) and
        (role eq entity.role) and
        (password eq entity.password) and
        (firstName eq entity.firstName) and
        (lastName eq entity.lastName) and
        (phone eq entity.phone) and
        (address eq entity.address) and
        (selection eq entity.selection)
    ).empty() }
}
