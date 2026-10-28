

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slisaconnect.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.theme.*

private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val timeFormat = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH)

fun LocalDate.pretty(): String = format(dateFormat)
fun Long.timeText(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(timeFormat)

// ---- small phone helpers ------------------------------------------------------------------------

/** Opens the share sheet with some text. Nothing is sent anywhere until the person picks an app. */
fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try { startActivity(Intent.createChooser(send, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: ActivityNotFoundException) { }
}

/** Opens the dialler with the number filled in. The person still has to press call. */
fun Context.dial(number: String) {
    val clean = number.filter { it.isDigit() || it == '+' }
    if (clean.isEmpty()) return
    try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: ActivityNotFoundException) { }
}

// ---- screen frame -------------------------------------------------------------------------------

/** Top bar plus a scrolling body. Every screen after sign-in uses it. */
@Composable
fun ScreenFrame(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (subtitle != null) {
                        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )
        val body = Modifier.weight(1f).fillMaxWidth().let { if (scroll) it.verticalScroll(rememberScrollState()) else it }
        Column(body.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}

/** The bell in the top bar, with the number of unread messages. */
@Composable
fun BellAction(count: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(badge = { if (count > 0) Badge { Text(if (count > 9) "9+" else "$count") } }) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = if (count > 0) "Notifications, $count unread" else "Notifications",
            )
        }
    }
}

// ---- cards and chips ----------------------------------------------------------------------------

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
        Text(
            text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, maxLines = 1,
        )
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

@Composable
fun BadChip(text: String) = PillChip(text, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)

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
fun Muted(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
fun Mono(text: String) =
    Text(text, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
fun ListRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    locked: Boolean = false,
    badge: Int = 0,
    emphasis: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val alpha = if (locked) 0.6f else 1f
    Panel(onClick = if (locked) null else onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (leading != null) {
                leading()
            } else if (locked) {
                IconBox(Icons.Default.Lock, alpha)
            } else if (icon != null) {
                IconBox(icon, alpha)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = if (emphasis) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                )
                if (subtitle != null) Muted(subtitle)
            }
            if (badge > 0) {
                Surface(color = Magenta, contentColor = Color.White, shape = CircleShape) {
                    Text("$badge", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            trailing?.invoke()
            if (onClick != null && !locked) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun IconBox(icon: ImageVector, alpha: Float) {
    Surface(
        shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
    ) { Icon(icon, null, Modifier.padding(8.dp).size(20.dp)) }
}

@Composable
fun Avatar(name: String, size: Int = 44) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, contentColor = Color.White, modifier = Modifier.size(size.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(), fontWeight = FontWeight.Bold)
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

/** Lays tiles out two to a row. */
@Composable
fun TileGrid(tiles: List<@Composable (Modifier) -> Unit>) {
    tiles.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            pair.forEach { tile -> tile(Modifier.weight(1f)) }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

/** A big coloured tile like the ones on the dashboard in the design. */
@Composable
fun ColourTile(title: String, subtitle: String, icon: ImageVector, colour: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.heightIn(min = 120.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        color = colour, contentColor = TileText, shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(34.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EmptyState(title: String, text: String) {
    Panel {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Muted(text)
    }
}

@Composable
fun AccessDenied(role: Role, onBack: () -> Unit) {
    Panel {
        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error)
        Text("This isn't part of your role", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Your role is ${role.label}. This page needs a different permission. Ask a platform admin if you think you should have it.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onBack) { Text("Go back") }
    }
}

// ---- buttons ------------------------------------------------------------------------------------

@Composable
fun PillButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
    ) { Text(text.uppercase(), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) }
}

@Composable
fun LimeButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = LimeText),
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

// ---- form fields --------------------------------------------------------------------------------

@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    error: String? = null,
    hint: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    password: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
) {
    var shown by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label) },
        isError = error != null,
        supportingText = { val msg = error ?: hint; if (msg != null) Text(msg) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        visualTransformation = if (password && !shown) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) {
            { TextButton(onClick = { shown = !shown }) { Text(if (shown) "Hide" else "Show") } }
        } else null,
    )
}

@Composable
fun DropdownField(label: String, value: String, options: List<String>, error: String? = null, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); open = false }) }
        }
    }
}

/** A date box that opens a calendar. [value] is null until a day is chosen. */
@Composable
fun DateField(label: String, value: LocalDate?, error: String? = null, modifier: Modifier = Modifier.fillMaxWidth(), onPick: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = value?.pretty() ?: "", onValueChange = {}, readOnly = true, label = { Text(label) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier.fillMaxWidth(),
        )
        // The text box swallows taps, so a see-through layer on top opens the calendar instead.
        Box(Modifier.matchParentSize().clickable { open = true })
    }
    if (open) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms -> onPick(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, hint: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label)
            if (hint != null) Muted(hint)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun FactRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FieldError(text: String?) {
    if (text != null) Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

/** A row of chips the person can tap to filter a list. */
@Composable
fun <T> FilterRow(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { o -> FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(label(o)) }) }
    }
}
