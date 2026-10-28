package com.example.slisaconnect.data


import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** A sign-in. The password itself is never stored, only a salted PBKDF2 hash. */
data class Account(val userId: String, val email: String, val salt: String, val hash: String)

class PasswordHasher(private val iterations: Int = 100_000) {
    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), iterations, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(key)
    }

    fun matches(password: String, salt: String, expected: String): Boolean =
        MessageDigest.isEqual(hash(password, salt).toByteArray(), expected.toByteArray())
}
