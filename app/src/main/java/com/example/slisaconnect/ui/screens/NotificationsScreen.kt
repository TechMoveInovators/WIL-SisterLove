package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.ui.*
import za.org.slisa.connect.ui.theme.Magenta

/** The inbox. Shared by both sides: users see event and team updates, staff see reviews waiting for them. */
@Composable
fun NotificationsScreen(nav: Navigator, staffView: Boolean, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val notices = state.myNotices
    ScreenFrame(
        title = if (staffView) "Alerts" else "Notifications",
        onBack = if (staffView) null else ({ nav.back() }),
        actions = { if (state.unreadCount > 0) TextButton(onClick = { vm.markAllNoticesRead() }) { Text("Mark all read") } },
    ) {
        if (notices.isEmpty()) {
            EmptyState("You're all caught up", if (staffView) "New teams, entries and challenges waiting for review show up here." else "Event announcements and team updates show up here.")
        }
        notices.forEach { n ->
            Panel(onClick = { vm.markNoticeRead(n.id); nav.open(n.link) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!n.read) PillChip("New", Magenta, androidx.compose.ui.graphics.Color.White)
                    PlainChip(n.kind.label)
                    Spacer(Modifier.weight(1f))
                    Muted(n.time.timeText())
                }
                Text(n.title, fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold)
                Muted(n.body)
            }
        }
    }
}
