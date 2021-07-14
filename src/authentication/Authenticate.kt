package com.vivek.notes.authentication

import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val ALGO_HMAC_SHA1 = "HmacSHA1"
private val hashKey = System.getenv("HASH_SECRET_KEY").toByteArray()
private val hmacKey = SecretKeySpec(hashKey, ALGO_HMAC_SHA1)

fun hash(password: String): String {
    val hmac = Mac.getInstance(ALGO_HMAC_SHA1)
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}