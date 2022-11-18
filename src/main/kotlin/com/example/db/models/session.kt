package com.example.db.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Session(val id: String, val value: String)

const val VALUE = "value"

object Sessions : Table() {
    val id = varchar(ID, 128)
    val value = varchar(VALUE, 1024)
    override val primaryKey = PrimaryKey(id)
}
