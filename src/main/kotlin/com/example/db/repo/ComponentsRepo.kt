package com.example.db.repo

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
import org.jetbrains.annotations.TestOnly
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object ComponentsRepo : AbsRepo<Component, Components, Int>(Components, id) {

    override fun resultRowToEntity(row: ResultRow): Component =
        Component(row[id], row[title], row[type], row[description], row[cost], row[image])

    suspend fun getAll(type: Type): List<Component> = getAllBy(Components.type eq type)

    override fun setValues(it: UpdateBuilder<Int>, entity: Component) {
        if (entity.id != null) it[id] = entity.id
        it[title] = entity.title
        it[type] = entity.type
        it[description] = entity.description
        it[cost] = entity.cost
        it[image] = entity.image
    }

    override suspend fun exactPresents(entity: Component): Boolean = dbQuery { !Components.select(
        (if (entity.id != null) Components.id eq entity.id else Op.TRUE) and
        (title eq entity.title) and
        (type eq entity.type) and
        (description eq entity.description) and
        (cost eq entity.cost) and
        (image eq entity.image)
    ).empty() }

    @TestOnly
    override fun testEntities(): Array<Component> = arrayOf(
        Component("Intel Core I5", Type.CPU, "Intel Core I5", 800, "intel_i5"),
        Component("AMD Ryzen 5 5600X", Type.CPU, "AMD Ryzen 5 5600X", 700, "amd_r5"),
        Component("AMD Ryzen 5 3600X", Type.CPU, "AMD Ryzen 5 3600X", 700, "amd_r5"),
        Component("Asus Prime Z590-P", Type.MB, "Asus Prime Z590-P", 400, "asus_prime_mb"),
        Component("Gigabyte Aorus X570 Elite", Type.MB, "Gigabyte Aorus X570 Elite", 450, "gigabyte_aorus_mb"),
        Component("Maxsun Radeon RX550", Type.GPU, "Maxsun Radeon RX550", 750, "maxsun_rx550_gpu"),
        Component("Asus GeForce GTX 1650", Type.GPU, "Asus GeForce GTX 1650", 750, "asus_1650_gpu"),
        Component("Micron Crucial DDR4 SODIMM 8Gb", Type.RAM, "Micron Crucial DDR4 SODIMM 8Gb", 300, "crucial_ram"),
        Component("GSkill DDR4 DIMM 16Gb", Type.RAM, "GSkill DDR4 DIMM 16Gb", 400, "gskill_ram"),
        Component("Hitachi 1Tb 250 Mb/sec", Type.HDD, "Hitachi 1Tb 250 Mb/sec", 200, "hitachi_hdd"),
        Component("MaxDigital 1Tb 250 Mb/sec", Type.HDD, "MaxDigital 1Tb 250 Mb/sec", 200, "maxdigital_hdd"),
        Component("Kingston SATA 1Tb 1000Mb/Sec", Type.SSD, "Kingston SATA 1Tb 1000Mb/Sec", 500, "kingston_ssd"),
        Component("Sabrent M2 PCIExpress 1Tb 1000Mb/Sec", Type.SSD, "Sabrent M2 PCIExpress 1Tb 1000Mb/Sec", 500, "sabrent_ssd"),
        Component("Corsair RM 850x 500W", Type.PSU, "Corsair RM 850x 500W", 200, "corsair_psu"),
        Component("Cooler Master MWE 750W", Type.PSU, "Cooler Master MWE 750W", 500, "cooler_master_psu"),
        Component("Corsair A50 AM4", Type.FAN, "Corsair A50 AM4", 100, "corsair_fan"),
        Component("Noctua R200 AM3", Type.FAN, "Noctua R200 AM3", 80, "noctua_fan"),
        Component("Musetex ALT 905", Type.CASE, "Musetex ALT 905", 125, "musetex_case"),
        Component("Phanteks S5", Type.CASE, "Phanteks S5", 200, "phanteks_case"),
        Component("Thermaltake ZTX", Type.CASE, "Thermaltake ZTX", 190, "thermaltake_case")
    )
}
