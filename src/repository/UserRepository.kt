package com.vivek.notes.repository

import com.vivek.notes.data.model.User
import com.vivek.notes.data.table.UserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class UserRepository {

    suspend fun addUser(user: User) {
        DatabaseFactory.dbQuery {
            UserTable.insert { table ->
                table[UserTable.email] = user.email
                table[UserTable.name] = user.userName
                table[UserTable.hashPassword] = user.hashPassword
            }
        }
    }

    suspend fun findUserByEmail(email: String) = DatabaseFactory.dbQuery {
        val row = UserTable.select { UserTable.email.eq(email) }
//            .map { rowToUser(it) }
            .singleOrNull()
        rowToUser(row)
    }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) return null

        return User(
            email = row[UserTable.email],
            userName = row[UserTable.name],
            hashPassword = row[UserTable.hashPassword]
        )
    }
}