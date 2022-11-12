package com.example.db.models

import org.jetbrains.exposed.sql.Table

const val VALUE = "value"

object Sessions : Table() {
    val id = varchar(ID, 128)
    val value = varchar(VALUE, 1024)
    override val primaryKey = PrimaryKey(id)
}
