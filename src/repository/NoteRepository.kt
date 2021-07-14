package com.vivek.notes.repository

import com.vivek.notes.data.model.Note
import com.vivek.notes.data.model.User
import com.vivek.notes.data.table.NoteTable
import com.vivek.notes.data.table.UserTable
import org.jetbrains.exposed.sql.*

class NoteRepository {

    suspend fun addNote(note: Note, email: String) {
        DatabaseFactory.dbQuery {
            NoteTable.insert { table ->
                table[NoteTable.id] = note.id
                table[NoteTable.title] = note.title
                table[NoteTable.description] = note.description
                table[NoteTable.date] = note.date
                table[NoteTable.email] = email
            }
        }
    }

    suspend fun getAllNotes(email: String): List<Note> = DatabaseFactory.dbQuery {
        NoteTable.select {
            NoteTable.email.eq(email)
        }.mapNotNull { rowToNote(it) }
    }

    suspend fun updateNote(note: Note, email: String) {
        DatabaseFactory.dbQuery {
            NoteTable.update(where = {
                NoteTable.email.eq(email) and NoteTable.id.eq(note.id)
            }) { table ->
                table[NoteTable.title] = note.title
                table[NoteTable.description] = note.description
                table[NoteTable.date] = note.date
            }
        }
    }

    suspend fun deleteNote(id: String, email: String) {
        DatabaseFactory.dbQuery {
            NoteTable.deleteWhere { NoteTable.email.eq(email) and NoteTable.id.eq(id) }
        }
    }

    private fun rowToNote(row: ResultRow?): Note? {
        if (row == null) return null

        return Note(
            id = row[NoteTable.id],
            title = row[NoteTable.title],
            description = row[NoteTable.description],
            date = row[NoteTable.date]
        )
    }
}