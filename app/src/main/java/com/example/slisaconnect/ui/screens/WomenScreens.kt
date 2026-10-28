package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*
import com.example.slisaconnect.ui.theme.*

/** Women's Base from the design: six tiles and a private "Need support?" panel. */
@Composable
fun WomenBaseScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ScreenFrame(title = "Women's Base", onBack = { nav.back() }, actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } }) {
        Text("Your Health. Your Rights. Your Voice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("How can we support you today?", fontWeight = FontWeight.SemiBold)
        val pink = Color(0xFFFFB3F7)
        TileGrid(
            listOf(
                { m -> ColourTile("Safety & Support", "GBVF awareness, safety planning and support resources.", Icons.Default.Lock, pink, m) { nav.to(Routes.SUPPORT) } },
                { m -> ColourTile("Get Involved", "Join initiatives and help create change.", Icons.Default.Favorite, pink, m) { nav.tab(Routes.VOLUNTEER) } },
                { m -> ColourTile("Opportunities", "Volunteering and personal growth.", Icons.Default.Star, pink, m) { nav.tab(Routes.VOLUNTEER) } },
                { m -> ColourTile("Community & Events", "Workshops, campaigns and women's gatherings.", Icons.Default.Person, pink, m) { nav.tab(Routes.EVENTS) } },
                { m -> ColourTile("Health & Wellness", "HIV, PrEP, sexual and reproductive health.", Icons.Default.Favorite, pink, m) { nav.tab(Routes.LEARN) } },
                { m -> ColourTile("Know Your Rights", "Learn about equality, consent and your rights.", Icons.Default.Info, pink, m) { nav.tab(Routes.LEARN) } },
            ),
        )
        NightPanel {
            Text("NEED SUPPORT?", fontWeight = FontWeight.Bold)
            Text(
                "Find verified support services and guidance. You can browse resources without sharing your personal story publicly.",
                style = MaterialTheme.typography.bodySmall,
            )
            LimeButton("Contact", Modifier.fillMaxWidth()) { nav.to(Routes.SUPPORT) }
        }
    }
}

/** Verified support contacts. Only contacts SLiSA staff have verified and published reach this screen. */
@Composable
fun SupportScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val published = state.resources.filter { it.status == ResourceStatus.PUBLISHED }
    ScreenFrame(title = "Safety & Support", onBack = { nav.back() }) {
        NoteBox("Nothing you do on this page is recorded or shared. Browsing resources never asks for your story.", NoteKind.GOOD)
        SectionTitle("National helplines")
        Helplines.all.forEach { h ->
            Panel(onClick = { ctx.dial(h.number) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(h.name, fontWeight = FontWeight.Bold)
                        Muted(h.note)
                    }
                    Text(h.number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Muted("Helplines last checked ${Helplines.CHECKED}.")
        ResourceKind.values().forEach { kind ->
            val items = published.filter { it.kind == kind }
            if (items.isNotEmpty()) {
                SectionTitle(kind.label)
                items.forEach { r ->
                    Panel {
                        Text(r.name, fontWeight = FontWeight.Bold)
                        if (r.notes.isNotBlank()) Muted(r.notes)
                        OutlinedButton(onClick = { ctx.dial(r.contact) }) { Text(r.contact) }
                        GoodChip("Verified by SLiSA")
                    }
                }
            }
        }
        if (published.isEmpty()) Muted("Local services will appear here once SLiSA has verified them.")
    }
}
