package com.vivek.notes

import com.vivek.notes.authentication.JWTService
import com.vivek.notes.authentication.hash
import com.vivek.notes.data.model.User
import com.vivek.notes.repository.DatabaseFactory
import com.vivek.notes.repository.NoteRepository
import com.vivek.notes.repository.UserRepository
import com.vivek.notes.routes.NoteRoutes
import com.vivek.notes.routes.UserRoutes
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.locations.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    DatabaseFactory.init()
    val repository = UserRepository()
    val noteRepository = NoteRepository()
    val jwtService = JWTService()
    val hashFunction = { s: String -> hash(s) }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt(name = "jwt") {
            verifier(jwtService.verifier)
            realm = "Note Server"
            validate {
                val payload = it.payload
                val email = payload.getClaim("email").asString()
                val user = repository.findUserByEmail(email)
                user
            }
        }

    }
    install(Locations) {

    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/token") {
            val email = call.request.queryParameters["email"]!!
            val password = call.request.queryParameters["password"]!!
            val username = call.request.queryParameters["username"]!!
            val user = User(email = email, hashPassword = hash(password), userName = username)
            call.respond(jwtService.generateToken(user))
        }
        UserRoutes(repository, jwtService, hashFunction)
        NoteRoutes(noteRepository, hashFunction)

//        get("/notes/{id}") {
//            val id = call.parameters["id"]
//            call.respond("$id")
//        }
//        get("/notes") {
//            val id = call.request.queryParameters["id"]
//            call.respond("$id")
//        }
//
//        route("/notes") {
//            route("/create") {
//                //localhost:8080/notes/create
//                post {
//                    val body = call.receive<String>()
//                    call.respond(body)
//                }
//            }
//
//            delete {
//                val body = call.receive<String>()
//                call.respond(body)
//            }
//        }


    }
}

data class MySession(val count: Int = 0)

