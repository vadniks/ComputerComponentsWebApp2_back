package com.example.models

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
    val image: String
) {
    constructor(title: String, type: Type, description: String, cost: Int, image: String)
    : this(null, title, type, description, cost, image)
}

fun Component.toJson() = Json.encodeToString(this)

fun String.jsonToComponent() = Json.decodeFromString<Component>(this)

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
    val title = varchar(TITLE, TITLE_LENGTH).index()
    val type = enumeration<Type>(TYPE)
    val description = varchar(DESCRIPTION, DESCRIPTION_LENGTH)
    val cost = integer(COST).index()
    val image = varchar(IMAGE, IMAGE_LENGTH).index()

    override val primaryKey = PrimaryKey(id)
}

const val TITLE_LENGTH = 128
const val DESCRIPTION_LENGTH = 512
const val IMAGE_LENGTH = 64
