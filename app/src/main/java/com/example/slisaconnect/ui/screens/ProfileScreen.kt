package com.example.slisaconnect.ui.screens

@file:OptIn(ExperimentalMaterial3Api::class)


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*
import za.org.slisa.connect.ui.theme.NightMuted

/** Visible to every signed-in user. Staff also get a link into the protected admin area. */
@Composable
fun ProfileScreen(onEdit: () -> Unit, onOpenAdmin: () -> Unit, onOpenTeams: () -> Unit, onOpenTeam: (String) -> Unit, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser
    val profile = state.profiles[me.id]
    val displayName = profile?.let { "${it.first} ${it.last}" } ?: me.name
    val myTeams = state.myTeams

    ScreenFrame(title = "My profile") {
        NightPanel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(displayName)
                Column {
                    Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("SLiSA Connect · ${me.role.label}", style = MaterialTheme.typography.bodySmall, color = NightMuted)
                }
            }
            LimeButton("Edit your profile", Modifier.fillMaxWidth(), onClick = onEdit)
        }

        SectionTitle("My teams", if (myTeams.isEmpty()) null else "${myTeams.size}")
        myTeams.forEach { t ->
            ListRow(
                title = t.name,
                subtitle = "${t.sport} · ${t.ageGroup} · ${t.reference}",
                onClick = { onOpenTeam(t.id) },
                trailing = { StatusChip(t.status) },
            )
        }
        ListRow(
            title = if (myTeams.isEmpty()) "Register your team" else "Register another team",
            subtitle = "Register once, keep one roster, enter many competitions",
            icon = Icons.Default.Add,
            onClick = onOpenTeams,
        )

        if (me.role.isStaff) {
            SectionTitle("Staff tools")
            ListRow("Admin dashboard", "Protected area. Your access is checked on every page.", Icons.Default.Lock, onClick = onOpenAdmin)
        }

        SectionTitle("Account & preferences")
        ListRow("Personal information", "Name and contact details", onClick = onEdit)
        ListRow("Notifications", "Approval updates and reminders", onClick = onEdit)
        ListRow("Privacy & security", "Password, permissions and data choices", onClick = { vm.toast("Privacy & security is outside the first prototype.") })
        ListRow("Accessibility", "Text size and display preferences", onClick = { vm.toast("Accessibility is outside the first prototype.") })
        ListRow("Help & contact", "Get assistance", onClick = { vm.toast("Help is outside the first prototype.") })
        ListRow("Log out", "Securely sign out of your account", Icons.AutoMirrored.Filled.ExitToApp, onClick = { vm.toast("Signed out (demo only).") })
    }
}

private val provinces = listOf(
    "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo",
    "Mpumalanga", "North West", "Northern Cape", "Western Cape",
)
private val ageGroups = listOf("Under 18", "18-24", "25-34", "35-44", "45+")

@Composable
fun EditProfileScreen(onDone: () -> Unit, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser
    val saved = state.profiles[me.id]
        ?: UserProfile(me.name.substringBefore(" "), me.name.substringAfter(" ", ""), "", "", me.province, "25-34")

    // Keyed on the user so switching demo users resets the form.
    var first by rememberSaveable(me.id) { mutableStateOf(saved.first) }
    var last by rememberSaveable(me.id) { mutableStateOf(saved.last) }
    var email by rememberSaveable(me.id) { mutableStateOf(saved.email) }
    var phone by rememberSaveable(me.id) { mutableStateOf(saved.phone) }
    var province by rememberSaveable(me.id) { mutableStateOf(saved.province) }
    var ageGroup by rememberSaveable(me.id) { mutableStateOf(saved.ageGroup) }
    var nApprovals by rememberSaveable(me.id) { mutableStateOf(saved.notifyApprovals) }
    var nReminders by rememberSaveable(me.id) { mutableStateOf(saved.notifyReminders) }
    var nEvents by rememberSaveable(me.id) { mutableStateOf(saved.notifyEvents) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    ScreenFrame(title = "Edit profile", onBack = onDone) {
        NoteBox("We only ask for what the app needs. No ID number, home address or date of birth.", NoteKind.GOOD)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(first, { first = it; errors = errors - "first" }, label = { Text("First name") }, singleLine = true,
                isError = "first" in errors, supportingText = { errors["first"]?.let { Text(it) } }, modifier = Modifier.weight(1f))
            OutlinedTextField(last, { last = it; errors = errors - "last" }, label = { Text("Last name") }, singleLine = true,
                isError = "last" in errors, supportingText = { errors["last"]?.let { Text(it) } }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(email, { email = it; errors = errors - "email" }, label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = "email" in errors, supportingText = { errors["email"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it; errors = errors - "phone" }, label = { Text("Phone (optional)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            supportingText = { Text(errors["phone"] ?: "Only used if you ask for a call.") }, isError = "phone" in errors,
            modifier = Modifier.fillMaxWidth())

        Dropdown("Province", province, provinces) { province = it }
        Dropdown("Age group", ageGroup, ageGroups) { ageGroup = it }

        Panel {
            SectionTitle("Notifications")
            SwitchRow("Team approval updates", nApprovals) { nApprovals = it }
            SwitchRow("Missing-document reminders", nReminders) { nReminders = it }
            SwitchRow("New events near me", nEvents) { nEvents = it }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                errors = Validators.profile(first, last, email, phone)
                if (errors.isEmpty()) {
                    val result = vm.updateProfile(
                        UserProfile(first.trim(), last.trim(), email.trim(), phone.trim(), province, ageGroup, nApprovals, nReminders, nEvents),
                    )
                    if (result is Outcome.Done) onDone()
                }
            },
        ) { Text("Save changes") }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Dropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); open = false }) }
        }
    }
}
