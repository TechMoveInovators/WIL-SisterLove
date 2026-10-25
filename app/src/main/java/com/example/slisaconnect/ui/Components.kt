@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slisaconnect.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.slisaconnect.BuildConfig
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.theme.*

private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("en", "ZA"))
fun LocalDate.pretty(): String = format(dateFormat)

/** Top bar plus a scrolling body. [route] shows the protected path, like a web address. */
@Composable
fun ScreenFrame(
    title: String,
    onBack: (() -> Unit)? = null,
    route: String? = null,
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (route != null) {
                        Text(route, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            },
            actions = { if (BuildConfig.DEBUG) DemoUserMenu() },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )
        val body = Modifier.weight(1f).fillMaxWidth().let { if (scroll) it.verticalScroll(rememberScrollState()) else it }
        Column(body.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}


@Composable
private fun DemoUserMenu(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Default.Person, contentDescription = "Switch demo user") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Text("Demo only: sign in as", Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.users.filter { it.active }.take(6).forEach { u ->
                DropdownMenuItem(
                    text = { Text("${u.name} · ${u.role.label}", fontWeight = if (u.id == state.currentUserId) FontWeight.Bold else FontWeight.Normal) },
                    onClick = { vm.switchUser(u.id); open = false },
                )
            }
        }
    }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier.fillMaxWidth().clip(shape).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = container,
        contentColor = contentColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun NightPanel(content: @Composable ColumnScope.() -> Unit) =
    Panel(container = Night, contentColor = NightText, content = content)

@Composable
fun PillChip(text: String, container: Color, content: Color) {
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(50)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun StatusChip(status: TeamStatus) {
    val (bg, fg) = statusColors(status)
    PillChip(status.label, bg, fg)
}

@Composable
fun PlainChip(text: String) =
    PillChip(text, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
fun GoodChip(text: String) { val (bg, fg) = goodColors(); PillChip(text, bg, fg) }

@Composable
fun WarnChip(text: String) { val (bg, fg) = warnColors(); PillChip(text, bg, fg) }

enum class NoteKind { NEUTRAL, WARN, BAD, GOOD }

@Composable
fun NoteBox(text: String, kind: NoteKind = NoteKind.NEUTRAL) {
    val (bg, fg) = when (kind) {
        NoteKind.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        NoteKind.WARN -> warnColors()
        NoteKind.BAD -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        NoteKind.GOOD -> goodColors()
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SectionTitle(text: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ListRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    locked: Boolean = false,
    badge: Int = 0,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val alpha = if (locked) 0.6f else 1f
    Panel(onClick = if (locked) null else onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                leading != null -> leading()
                icon != null || locked -> Surface(
                    shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                ) { Icon(if (locked) Icons.Default.Lock else icon!!, null, Modifier.padding(8.dp).size(20.dp)) }
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (badge > 0) {
                Surface(color = Magenta, contentColor = Color.White, shape = CircleShape) {
                    Text("$badge", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            trailing?.invoke()
            if (onClick != null && !locked) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Avatar(name: String, size: Int = 44) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, contentColor = Color.White, modifier = Modifier.size(size.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(color = Night, contentColor = NightText, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = NightMuted)
        }
    }
}

@Composable
fun AccessDenied(role: Role, onBack: () -> Unit) {
    Panel {
        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error)
        Text("You don't have access to this area", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Your role is ${role.label}. This page needs a different permission. Ask a platform admin if you think you should have it.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onBack) { Text("Back to my profile") }
    }
}

@Composable
fun FieldError(text: String?) {
    if (text != null) Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun LimeButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = LimeText),
    ) { Text(text, fontWeight = FontWeight.Bold) }
}
