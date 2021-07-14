package com.vivek.notes.routes

import com.vivek.notes.authentication.JWTService
import com.vivek.notes.data.model.Note
import com.vivek.notes.data.model.User
import com.vivek.notes.data.model.requests.RegisterRequest
import com.vivek.notes.data.model.response.BaseResponse
import com.vivek.notes.repository.NoteRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

object NoteConstants {
    const val API_VERSION = "/v1"
    const val NOTES = "$API_VERSION/notes"
    const val CREATE_NOTE = "$NOTES/create"
    const val UPDATE_NOTE = "$NOTES/update"
    const val DELETE_NOTE = "$NOTES/delete"
}

@Location(NoteConstants.NOTES)
class NoteGetRoute

@Location(NoteConstants.CREATE_NOTE)
class NoteCreateRoute

@Location(NoteConstants.UPDATE_NOTE)
class NoteUpdateRoute

@Location(NoteConstants.DELETE_NOTE)
class NoteDeleteRoute


fun Route.NoteRoutes(noteRepository: NoteRepository, hashFunction: (String) -> String) {


    authenticate("jwt") {

        post<NoteCreateRoute> {
            val noteRequest = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, BaseResponse(success = false, message = "Bad request"))
                return@post
            }

            try {
                val email = call.principal<User>()?.email
                noteRepository.addNote(note = noteRequest, email = email!!)
                call.respond(HttpStatusCode.OK, BaseResponse(success = true, message = "Note created"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    BaseResponse(success = false, message = e.message ?: "Something went wrong")
                )
                return@post
            }
        }

        get<NoteGetRoute> {
            try {
                val email = call.principal<User>()?.email
                val notes = noteRepository.getAllNotes(email!!)
                call.respond(HttpStatusCode.OK, notes)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    emptyList<Note>()
                )
            }
        }

        post<NoteUpdateRoute> {
            val noteRequest = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, BaseResponse(success = false, message = "Bad request"))
                return@post
            }

            try {
                val email = call.principal<User>()?.email
                noteRepository.updateNote(note = noteRequest, email = email!!)
                call.respond(HttpStatusCode.OK, BaseResponse(success = true, message = "Note updated"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    BaseResponse(success = false, message = e.message ?: "Something went wrong")
                )
                return@post
            }
        }


        delete<NoteDeleteRoute> {
            val noteId = try {
                call.request.queryParameters["id"]!!
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse(success = false, message = "QueryParam:id is missing")
                )
                return@delete
            }

            try {
                val email = call.principal<User>()?.email
                noteRepository.deleteNote(id = noteId, email = email!!)
                call.respond(HttpStatusCode.OK, BaseResponse(success = true,message = "Note deleted"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    BaseResponse(success = false, message = e.message ?: "Something went wrong")
                )
                return@delete
            }


        }

    }
}