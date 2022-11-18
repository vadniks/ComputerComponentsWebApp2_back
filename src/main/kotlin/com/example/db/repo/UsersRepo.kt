package com.example.db.repo

import com.example.db.DatabaseFactory.dbQuery
import com.example.db.models.Role
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
import org.jetbrains.annotations.TestOnly
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object UsersRepo : AbsRepo<User, Users, Int>(Users, id) {

    override fun resultRowToEntity(row: ResultRow): User = User(
        row[id],       row[name],      row[role],
        row[password], row[firstName], row[lastName],
        row[phone],    row[address],   row[selection]
    )

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

    @TestOnly
    override fun testEntities(): Array<User> = arrayOf(
        User("admin", Role.ADMIN, "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"),
        User("user", Role.USER, "04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb")
    )

    suspend fun checkRole(id: Int, role: Role): Boolean = getSingle(Users.id eq id, Users.role) == role

    suspend fun checkCredentials(name: String, password: String): Int? =
        getSingle((Users.name eq name) and (Users.password eq password), id)

    suspend fun setSelection(id: Int, selection: String?): Boolean =
        updateSingle(Users.id eq id, Users.selection, selection)

    suspend fun getSelection(id: Int): String? = getSingle(Users.id eq id, selection)

    suspend fun nameExists(name: String): Boolean = getSingle(Users.name eq name, id) != null

    suspend fun getName(id: Int): String? = getSingle(Users.id eq id, name)
}
