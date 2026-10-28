
package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*

@Composable
fun ProfileScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser
    if (me == null) {
        LaunchedEffect(Unit) { nav.to(Routes.WELCOME) }
        return
    }
    val profile = state.profiles[me.id]
    var confirmOut by remember { mutableStateOf(false) }

    ScreenFrame(title = "My Profile", actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } }) {
        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(me.name, 60)
                Column {
                    Text(me.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Muted(profile?.email ?: "")
                    Muted(listOfNotNull(me.province.ifBlank { null }, profile?.ageGroup?.ifBlank { null }).joinToString(" · "))
                }
            }
            if (me.role.isStaff) PlainChip(me.role.label)
        }
        SectionTitle("My activities")
        ListRow("My teams", "${state.myTeams.size} registered", Icons.Default.Star, onClick = { nav.to(Routes.YOUTH) })
        ListRow("My events", "Events you've responded to", Icons.Default.DateRange, onClick = { nav.to(Routes.MY_EVENTS) })
        ListRow("Notifications", if (state.unreadCount > 0) "${state.unreadCount} unread" else "All caught up", Icons.Default.Notifications, badge = state.unreadCount, onClick = { nav.to(Routes.NOTIFICATIONS) })

        SectionTitle("Account")
        ListRow("Edit profile", "Name, contact and notification choices", Icons.Default.Edit, onClick = { nav.to(Routes.EDIT_PROFILE) })
        ListRow("Privacy & security", "Change password, delete account", Icons.Default.Lock, onClick = { nav.to(Routes.SECURITY) })
        ListRow("Help & contact", "Helplines and support", Icons.Default.Info, onClick = { nav.to(Routes.HELP) })
        if (me.role.isStaff) ListRow("Back to admin", me.role.label, Icons.Default.Settings, onClick = { nav.to(Routes.ADMIN) })
        OutlinedButton(onClick = { confirmOut = true }, Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null); Spacer(Modifier.width(8.dp)); Text("Log out")
        }
    }
    if (confirmOut) SignOutDialog(onDismiss = { confirmOut = false }) { vm.signOut() }
}

@Composable
fun SignOutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log out?") },
        text = { Text("You'll need your email and password to get back in.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Log out") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun EditProfileScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser ?: return
    val p = state.profiles[me.id] ?: return
    var first by rememberSaveable { mutableStateOf(p.first) }
    var last by rememberSaveable { mutableStateOf(p.last) }
    var email by rememberSaveable { mutableStateOf(p.email) }
    var phone by rememberSaveable { mutableStateOf(p.phone) }
    var province by rememberSaveable { mutableStateOf(p.province) }
    var age by rememberSaveable { mutableStateOf(p.ageGroup) }
    var approvals by rememberSaveable { mutableStateOf(p.notifyApprovals) }
    var reminders by rememberSaveable { mutableStateOf(p.notifyReminders) }
    var events by rememberSaveable { mutableStateOf(p.notifyEvents) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    ScreenFrame(title = "Edit Profile", onBack = { nav.back() }) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field(first, { first = it; errors = errors - "first" }, "Name", Modifier.weight(1f), error = errors["first"])
            Field(last, { last = it; errors = errors - "last" }, "Last name", Modifier.weight(1f), error = errors["last"])
        }
        Field(email, { email = it; errors = errors - "email" }, "Email address", error = errors["email"], keyboard = KeyboardType.Email)
        Field(phone, { phone = it; errors = errors - "phone" }, "Phone number (optional)", error = errors["phone"], keyboard = KeyboardType.Phone)
        DropdownField("Province", province, Validators.provinces) { province = it }
        DropdownField("Age group", age, Validators.ageGroups) { age = it }
        NoteBox("We keep an age group and province, not your date of birth or home address.", NoteKind.GOOD)

        SectionTitle("Notifications")
        SwitchRow("Team updates", approvals, "Approvals, requests for changes, entry decisions") { approvals = it }
        SwitchRow("Reminders", reminders, "Missing consent forms and deadlines") { reminders = it }
        SwitchRow("New events and learning", events, "When SLiSA publishes something new") { events = it }

        PillButton("Save", Modifier.fillMaxWidth()) {
            errors = Validators.profile(first, last, email, phone)
            if (errors.isEmpty()) {
                val out = vm.updateProfile(UserProfile(first.trim(), last.trim(), email.trim(), phone.trim(), province, age, approvals, reminders, events))
                if (out is Outcome.Done) nav.back()
            }
        }
    }
}

@Composable
fun PrivacySecurityScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var delPassword by remember { mutableStateOf("") }
    var delError by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    ScreenFrame(title = "Privacy & security", onBack = { nav.back() }) {
        SectionTitle("Change password")
        Field(current, { current = it; error = null }, "Current password", password = true)
        Field(new, { new = it; error = null }, "New password", password = true, hint = "At least 8 characters, with letters and numbers.")
        Field(confirm, { confirm = it; error = null }, "Confirm new password", password = true, imeAction = ImeAction.Done)
        FieldError(error)
        PillButton("Change password", Modifier.fillMaxWidth()) {
            error = when {
                new != confirm -> "The new passwords don't match."
                else -> Validators.password(new)
            }
            if (error == null) vm.changePassword(current, new) { out ->
                if (out is Outcome.Failed) error = out.message else { current = ""; new = ""; confirm = "" }
            }
        }

        SectionTitle("Your data")
        NoteBox("Your account and roster details are stored on this phone only. Nothing is uploaded.")

        SectionTitle("Delete account")
        Muted("This removes your account and the teams you manage. It can't be undone.")
        Field(delPassword, { delPassword = it; delError = null }, "Enter your password to confirm", password = true, imeAction = ImeAction.Done)
        FieldError(delError)
        OutlinedButton(onClick = { confirmDelete = true }, Modifier.fillMaxWidth(), enabled = delPassword.isNotEmpty()) { Text("Delete my account") }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete your account?") },
            text = { Text("Your profile and the teams you manage will be removed from this phone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAccount(delPassword) { out -> if (out is Outcome.Failed) delError = out.message }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun HelpScreen(nav: Navigator) {
    val ctx = LocalContext.current
    ScreenFrame(title = "Help & contact", onBack = { nav.back() }) {
        Muted("If you are in danger, call the police or 112 first.")
        Helplines.all.forEach { h ->
            Panel(onClick = { ctx.dial(h.number) }) {
                Text(h.name, fontWeight = FontWeight.Bold)
                Muted(h.note)
                Text(h.number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Muted("Last checked ${Helplines.CHECKED}.")
        ListRow("More support contacts", "Verified local services", Icons.Default.Favorite, onClick = { nav.to(Routes.SUPPORT) })
    }
}
