package com.example.slisaconnect.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** A sign-in. The password itself is never stored, only a salted PBKDF2 hash. */
data class Account(val userId: String, val email: String, val salt: String, val hash: String)

class PasswordHasher(private val iterations: Int = 10_000) {

    private fun getKeyFactory(): SecretKeyFactory {
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        } catch (e: Exception) {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }
    }

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64Compat.encode(bytes)
    }

    fun hash(password: String, salt: String): String {
        return try {
            val decodedSalt = Base64Compat.decode(salt)
            val spec = PBEKeySpec(password.toCharArray(), decodedSalt, iterations, 256)
            val factory = getKeyFactory()
            val key = factory.generateSecret(spec).encoded
            Base64Compat.encode(key)
        } catch (e: Exception) {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(Base64Compat.decode(salt))
            Base64Compat.encode(md.digest(password.toByteArray(Charsets.UTF_8)))
        }
    }

    fun matches(password: String, salt: String, expected: String): Boolean = try {
        MessageDigest.isEqual(hash(password, salt).toByteArray(Charsets.UTF_8), expected.toByteArray(Charsets.UTF_8))
    } catch (e: Exception) {
        false
    }

    private object Base64Compat {
        fun encode(bytes: ByteArray): String {
            return try {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } catch (e: Throwable) {
                java.util.Base64.getEncoder().encodeToString(bytes)
            }
        }

        fun decode(str: String): ByteArray {
            return try {
                android.util.Base64.decode(str, android.util.Base64.NO_WRAP)
            } catch (e: Throwable) {
                java.util.Base64.getDecoder().decode(str)
            }
        }
    }
}
