package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.R
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*
import com.example.slisaconnect.ui.theme.authBackground

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp

/** The Home Page in the design: logo, tagline and Sign In / Register choices. */
@Composable
fun WelcomeScreen(nav: Navigator) {
    Column(
        Modifier.fillMaxSize().background(authBackground()).systemBarsPadding().padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("SLiSA Connect", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.weight(1f))
        Image(painter = painterResource(R.drawable.slisa_logo), contentDescription = "SLiSA Connect logo", modifier = Modifier.height(250.dp))
        Spacer(Modifier.height(22.dp))
        Text(
            "Learn & grow SRHR, GBVF, HIV & PrEP",
            fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.weight(1f))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton("Sign In", Modifier.fillMaxWidth()) { nav.to(Routes.SIGN_IN) }
            OutlinedButton(
                onClick = { nav.to(Routes.SIGN_UP) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(50),
            ) {
                Text("Register Account".uppercase(), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
    }
}

@Composable
fun SignInScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var stay by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var forgot by remember { mutableStateOf(false) }

    fun submit() {
        if (busy) return
        error = when {
            email.isBlank() -> "Enter your email."
            password.isEmpty() -> "Enter your password."
            else -> null
        }
        if (error != null) return
        busy = true
        vm.signIn(email, password, stay) { out ->
            busy = false
            if (out is Outcome.Failed) {
                error = out.message
            } else if (out is Outcome.Done) {
                val isStaff = vm.state.value.currentUser?.role?.isStaff == true
                nav.to(if (isStaff) Routes.ADMIN else Routes.HOME)
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(authBackground()).systemBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Image(painter = painterResource(R.drawable.slisa_logo), contentDescription = "SLiSA Connect logo", modifier = Modifier.height(120.dp))
        Text("Sign In", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        Field(email, { email = it; error = null }, "Email", keyboard = KeyboardType.Email)
        Field(password, { password = it; error = null }, "Password", password = true, imeAction = ImeAction.Done, onDone = { submit() })

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = stay, onCheckedChange = { stay = it })
                Text("Remember me", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { forgot = true }) {
                Text("Forgot Password?", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
        FieldError(error)

        PillButton(if (busy) "Signing in…" else "Sign in", Modifier.fillMaxWidth(), enabled = !busy) { submit() }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an Account?", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { nav.to(Routes.SIGN_UP) }) { Text("Sign Up", fontWeight = FontWeight.Bold) }
        }
    }

    if (forgot) {
        AlertDialog(
            onDismissRequest = { forgot = false },
            title = { Text("Forgot your password?") },
            text = {
                Text(
                    "Your account is stored on this phone, so there is no email to send a reset link to yet. " +
                            "Try your usual passwords, or create a new account with a different email. " +
                            "Resetting by email arrives when SLiSA Connect goes online.",
                )
            },
            confirmButton = { TextButton(onClick = { forgot = false }) { Text("OK") } },
        )
    }
}

@Composable
fun SignUpScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var ageGroup by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var problem by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun submit() {
        if (busy) return
        val form = SignUpForm(first, last, ageGroup, province, phone, email.trim(), password, confirm)
        errors = Validators.signUp(form)
        problem = null
        if (errors.isNotEmpty()) return
        busy = true
        vm.signUp(form) { out ->
            busy = false
            if (out is Outcome.Failed) {
                problem = out.message
            } else if (out is Outcome.Done) {
                val isStaff = vm.state.value.currentUser?.role?.isStaff == true
                nav.to(if (isStaff) Routes.ADMIN else Routes.HOME)
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(authBackground()).systemBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Spacer(Modifier.weight(1f))
            Image(painter = painterResource(R.drawable.slisa_logo), contentDescription = "SLiSA Connect logo", modifier = Modifier.height(80.dp))
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }
        Text("Register", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        if (vm.isFreshInstall && vm.firstAccountBecomesAdmin) {
            NoteBox("You're the first person to sign up on this phone, so your account will be the platform admin. Everyone who signs up after you is a member.", NoteKind.WARN)
        }
        NoteBox("We don't ask for your date of birth or home address. An age group and province are enough to show you the right things.", NoteKind.GOOD)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field(first, { first = it; errors = errors - "first" }, "Name", Modifier.weight(1f), error = errors["first"])
            Field(last, { last = it; errors = errors - "last" }, "Last name", Modifier.weight(1f), error = errors["last"])
        }
        DropdownField("Age group", ageGroup, Validators.ageGroups, errors["ageGroup"]) { ageGroup = it; errors = errors - "ageGroup" }
        DropdownField("Province", province, Validators.provinces, errors["province"]) { province = it; errors = errors - "province" }
        Field(phone, { phone = it; errors = errors - "phone" }, "Phone number (optional)", keyboard = KeyboardType.Phone, error = errors["phone"])
        Field(email, { email = it; errors = errors - "email" }, "Email address", keyboard = KeyboardType.Email, error = errors["email"])
        Field(password, { password = it; errors = errors - "password" }, "Create password", password = true, error = errors["password"], hint = "At least 8 characters, with letters and numbers.")
        Field(confirm, { confirm = it; errors = errors - "confirm" }, "Confirm password", password = true, error = errors["confirm"], imeAction = ImeAction.Done, onDone = { submit() })
        FieldError(problem)

        PillButton(if (busy) "Creating account…" else "Submit", Modifier.fillMaxWidth(), enabled = !busy) { submit() }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { nav.to(Routes.SIGN_IN) }) { Text("Sign In", fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(8.dp))
    }
}
