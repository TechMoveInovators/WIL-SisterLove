@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*

/** Support contacts: added, then verified, then published. Users only ever see published ones. */
@Composable
fun SupportManagementScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    val edit = state.can(Permission.SUPPORT_EDIT)
    val verify = state.can(Permission.REFERRALS_VERIFY)
    val publish = state.can(Permission.SUPPORT_PUBLISH)

    ScreenFrame(title = "Women's Hub & Support", onBack = { nav.back() }) {
        NoteBox("Contacts must be verified before they are published. Only a platform admin can publish.", NoteKind.WARN)
        if (edit) PillButton("Add contact", Modifier.fillMaxWidth()) { adding = true }
        if (state.resources.isEmpty()) EmptyState("No contacts yet", "Add a service, verify it, then an admin publishes it to the Women's Base.")
        state.resources.forEach { r ->
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(r.name, fontWeight = FontWeight.Bold)
                        Muted("${r.kind.label} · ${r.contact}")
                    }
                    when (r.status) {
                        ResourceStatus.PUBLISHED -> GoodChip("Published")
                        ResourceStatus.VERIFIED -> WarnChip("Verified")
                        ResourceStatus.SUBMITTED -> PlainChip("Submitted")
                    }
                }
                if (r.notes.isNotBlank()) Muted(r.notes)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (verify && r.status == ResourceStatus.SUBMITTED) Button(onClick = { vm.verifyResource(r.id) }) { Text("Mark verified") }
                    if (publish && r.status == ResourceStatus.VERIFIED) Button(onClick = { vm.publishResource(r.id) }) { Text("Publish") }
                    if (publish && r.status == ResourceStatus.PUBLISHED) OutlinedButton(onClick = { vm.unpublishResource(r.id) }) { Text("Unpublish") }
                    if (publish) TextButton(onClick = { vm.deleteResource(r.id) }) { Text("Delete") }
                }
            }
        }
    }

    if (adding) {
        ModalBottomSheet(onDismissRequest = { adding = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            var name by rememberSaveable { mutableStateOf("") }
            var kind by rememberSaveable { mutableStateOf(ResourceKind.SAFETY) }
            var contact by rememberSaveable { mutableStateOf("") }
            var notes by rememberSaveable { mutableStateOf("") }
            var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Add contact", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Field(name, { name = it; errors = errors - "name" }, "Service name", error = errors["name"])
                FilterRow(ResourceKind.values().toList(), kind, { it.label }) { kind = it }
                Field(contact, { contact = it; errors = errors - "contact" }, "Phone or contact", error = errors["contact"], keyboard = KeyboardType.Phone)
                Field(notes, { notes = it }, "Notes (hours, area)", singleLine = false, minLines = 2)
                NoteBox("Don't enter anyone's personal story. Contact details of services only.")
                PillButton("Save", Modifier.fillMaxWidth()) {
                    errors = Validators.resource(name, contact)
                    if (errors.isEmpty() && vm.addResource(name, kind, contact, notes) is Outcome.Done) adding = false
                }
            }
        }
    }
}
