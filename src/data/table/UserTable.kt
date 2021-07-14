package com.vivek.notes.data.table

import org.jetbrains.exposed.sql.Table

object UserTable : Table() {
    val email = varchar("email", 512)
    val name = varchar("name", 512)
    val hashPassword = varchar("hash_password", 512)

    override val primaryKey: PrimaryKey = PrimaryKey(email)
}