package com.vivek.notes.routes

import com.vivek.notes.authentication.JWTService
import com.vivek.notes.data.model.User
import com.vivek.notes.data.model.requests.LoginRequest
import com.vivek.notes.data.model.requests.RegisterRequest
import com.vivek.notes.data.model.response.BaseResponse
import com.vivek.notes.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"

@Location(REGISTER_REQUEST)
class UserRegisterRoute

@Location(LOGIN_REQUEST)
class UserLoginRoute

fun Route.UserRoutes(repository: UserRepository, jwtService: JWTService, hasFunction: (String) -> String) {

    post<UserRegisterRoute> {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, BaseResponse(success = false, message = "Bad request"))
            return@post
        }

        try {
            val user = User(
                email = registerRequest.email,
                hasFunction(registerRequest.password),
                userName = registerRequest.username
            )
            repository.addUser(user = user)
            call.respond(HttpStatusCode.OK, BaseResponse(success = true, jwtService.generateToken(user)))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Conflict,
                BaseResponse(success = false, message = e.message ?: "Something went wrong")
            )
            return@post
        }
    }

    post<UserLoginRoute> {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, BaseResponse(success = false, message = "Bad request"))
            return@post
        }

        try {
            val user = repository.findUserByEmail(loginRequest.email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, BaseResponse(success = false, message = "user with email ${loginRequest.email} not found"))
            } else {
                if (user.hashPassword == hasFunction(loginRequest.password)) {
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse(success = true, message = jwtService.generateToken(user))
                    )
                } else {
                    call.respond(HttpStatusCode.NotFound, BaseResponse(success = false, message = "Wrong password"))
                }
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Conflict,
                BaseResponse(success = false, message = e.message ?: "Something went wrong")
            )
            return@post
        }
    }
}