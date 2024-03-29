package com.example.db.models

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int?,
    val name: String,
    val role: Role,
    val password: String,
    val firstName: String?,
    val lastName: String?,
    val phone: Int?,
    val address: String?,
    val selection: String?,
    val selections: String?
) {
    constructor(name: String, role: Role, password: String) : this(
        null,
        name, role, password,
        null, null, null, null, null, null
    )
}

@Serializable
data class UserDetails(val firstName: String, val lastName: String, val phone: Int, val address: String)

@Serializable
data class UserCredentials(val name: String, val password: String)

const val NAME = "name"
const val ROLE = "role"
const val PASSWORD = "password"
const val FIRST_NAME = "firstName"
const val LAST_NAME = "lastName"
const val PHONE = "phone"
const val ADDRESS = "address"
const val SELECTION = "selection"
const val SELECTIONS = "selections"

enum class Role(val role: Int) { USER(0), ADMIN(1) }

object Users : Table() {
    val id = integer(ID).autoIncrement()
    val name = varchar(NAME, LENGTH_MIDDLE).uniqueIndex()
    val role = enumeration<Role>(ROLE)
    val password = varchar(PASSWORD, LENGTH_MIDDLE)
    val firstName = varchar(FIRST_NAME, LENGTH_MIDDLE).nullable().index()
    val lastName = varchar(LAST_NAME, LENGTH_MIDDLE).nullable().index()
    val phone = integer(PHONE).nullable().uniqueIndex()
    val address = varchar(ADDRESS, LENGTH_LONG).nullable().index()
    val selection = varchar(SELECTION, LENGTH_MIDDLE).nullable()
    val selections = text(SELECTIONS).nullable()

    override val primaryKey = PrimaryKey(id)
}
