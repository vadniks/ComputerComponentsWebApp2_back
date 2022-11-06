package com.example.db.models

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class Component(
    val id: Int?,
    val title: String,
    val type: Type,
    val description: String,
    val cost: Int,
    val image: String?
) {
    constructor(title: String, type: Type, description: String, cost: Int, image: String?)
    : this(null, title, type, description, cost, image)
}

fun Component.toJson() = Json.encodeToString(this)

fun String.toComponent() = Json.decodeFromString<Component>(this)

fun List<Component>.toJson() = Json.encodeToString(this)

const val ID = "id"
const val TITLE = "title"
const val TYPE = "type"
const val DESCRIPTION = "description"
const val COST = "cost"
const val IMAGE = "image"

enum class Type(val type: Int, val title: String) {
    CPU (0, "Processor"),
    MB  (1, "Motherboard"),
    GPU (2, "Graphics adapter"),
    RAM (3, "Operating memory"),
    HDD (4, "Hard drive"),
    SSD (5, "Solid state drive"),
    PSU (6, "Power supply unit"),
    FAN (7, "Cooler"),
    CASE(8, "Case")
}

const val TYPE_AMOUNT = 9

object Components : Table() {
    val id = integer(ID).autoIncrement()
    val title = varchar(TITLE, LENGTH_MIDDLE).index()
    val type = enumeration<Type>(TYPE)
    val description = varchar(DESCRIPTION, LENGTH_LONG)
    val cost = integer(COST).index()
    val image = varchar(IMAGE, LENGTH_SHORT).index().nullable()

    override val primaryKey = PrimaryKey(id)
}

const val LENGTH_SHORT = 64
const val LENGTH_MIDDLE = 128
const val LENGTH_LONG = 512
