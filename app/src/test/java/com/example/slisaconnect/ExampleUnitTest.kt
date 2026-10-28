package com.example.slisaconnect

import com.example.slisaconnect.data.AppRepository
import com.example.slisaconnect.data.MemoryStore
import com.example.slisaconnect.model.Outcome
import com.example.slisaconnect.model.SignUpForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun signUpAndSignIn_caseInsensitiveAndTrimmed() {
        val repo = AppRepository(store = MemoryStore(), firstAccountIsAdmin = true)

        // 1. Sign up with leading/trailing spaces and mixed-case email
        val form = SignUpForm(
            first = "Jane",
            last = "Doe",
            ageGroup = "18-24",
            province = "Gauteng",
            phone = "0825550123",
            email = "  Jane.Doe@Example.Com  ",
            password = "Password123",
            confirm = "Password123",
        )
        val signUpResult = repo.signUp(form)
        assertTrue(signUpResult is Outcome.Done)

        // 2. Attempt duplicate sign up with different case and spaces
        val duplicateForm = form.copy(email = "jane.doe@example.com")
        val duplicateResult = repo.signUp(duplicateForm)
        assertTrue(duplicateResult is Outcome.Failed)
        assertEquals("An account with this email already exists. Try signing in.", duplicateResult.message)

        // 3. Sign in with lowercase email
        val signInResult = repo.signIn("jane.doe@example.com", "Password123", remember = true)
        assertTrue(signInResult is Outcome.Done)

        // 4. Sign in with wrong password
        val wrongPasswordResult = repo.signIn("jane.doe@example.com", "WrongPassword1", remember = true)
        assertTrue(wrongPasswordResult is Outcome.Failed)
        assertEquals("Email or password is incorrect.", wrongPasswordResult.message)
    }
}
