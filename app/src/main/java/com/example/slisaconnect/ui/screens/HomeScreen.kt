package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.R
import com.example.slisaconnect.ui.*
import com.example.slisaconnect.ui.theme.NightMuted

/** Home tab. The Admin entry only appears for staff roles. */
@Composable
fun HomeScreen(onOpenAdmin: () -> Unit, onOpenYouth: () -> Unit, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser
    ScreenFrame(title = "Home") {
        NightPanel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Image(
                    painter = painterResource(R.drawable.slisa_logo),
                    contentDescription = "SLiSA Connect logo",
                    modifier = Modifier.height(72.dp),
                )
                Column {
                    Text("Welcome", style = MaterialTheme.typography.bodySmall, color = NightMuted)
                    Text(me.name.substringBefore(" "), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Who are you? Choose how you'll use SLiSA Connect.", style = MaterialTheme.typography.bodySmall, color = NightMuted)
                }
            }
        }
        ListRow("Youth", "Register your team, events, learning", Icons.Default.Star, onClick = onOpenYouth)
        ListRow("Women", "Safety, health, support", Icons.Default.Favorite, onClick = { vm.toast("Women is outside the first prototype.") })
        ListRow("Volunteer", "Tasks, events, hours", Icons.Default.Person, onClick = { vm.toast("Volunteer is outside the first prototype.") })
        if (me.role.isStaff) {
            ListRow("Admin", "Staff access only", Icons.Default.Lock, onClick = onOpenAdmin)
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    ScreenFrame(title = name) {
        Panel {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "This tab is outside the first prototype. The six admin and account screens are ready to try from Profile.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
