package com.example.db.models

import org.jetbrains.exposed.sql.Table

@kotlinx.serialization.Serializable
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

data class Selection(
    var cpu: Int? = null, var mb: Int? = null, var gpu: Int? = null,
    var ram: Int? = null, var hdd: Int? = null, var ssd: Int? = null,
    var psu: Int? = null, var fan: Int? = null, var case: Int? = null
) { override fun toString() = "$cpu,$mb,$gpu,$ram,$hdd,$ssd,$psu,$fan,$case" }

fun String.toSelection(): Selection? {
    val values = split(',')
    if (values.isEmpty()) return null

    val v = { index: Int -> values[index].toIntOrNull() }
    return Selection(v(0), v(1), v(2), v(3), v(4), v(5), v(6), v(7), v(8))
}

infix operator fun Selection.get(type: Type): Int? = when (type) {
    Type.CPU -> cpu; Type.MB -> mb; Type.GPU -> gpu
    Type.RAM -> ram; Type.HDD -> hdd; Type.SSD -> ssd
    Type.PSU -> psu; Type.FAN -> fan; Type.CASE -> case
}

operator fun Selection.set(type: Type, id: Int) = when (type) {
    Type.CPU -> cpu = id; Type.MB -> mb = id; Type.GPU -> gpu = id
    Type.RAM -> ram = id; Type.HDD -> hdd = id; Type.SSD -> ssd = id
    Type.PSU -> psu = id; Type.FAN -> fan = id; Type.CASE -> case = id
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
