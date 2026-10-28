
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*

/** Articles for the Learn tab. Publishing notifies users who want updates. */
@Composable
fun LearningManagementScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Article?>(null) }
    var deleting by remember { mutableStateOf<Article?>(null) }

    ScreenFrame(title = "Learning Content", onBack = { nav.back() }) {
        PillButton("New article", Modifier.fillMaxWidth()) { creating = true }
        if (state.articles.isEmpty()) EmptyState("No articles yet", "Write one and publish it to the Learn tab.")
        state.articles.sortedByDescending { it.date }.forEach { a ->
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(a.title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    if (a.published) GoodChip("Published") else WarnChip("Draft")
                }
                Muted("${a.topic.label} · ${a.date.pretty()}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { editing = a }) { Text("Edit") }
                    OutlinedButton(onClick = { vm.setArticlePublished(a.id, !a.published) }) { Text(if (a.published) "Unpublish" else "Publish") }
                    TextButton(onClick = { deleting = a }) { Text("Delete") }
                }
            }
        }
    }

    if (creating || editing != null) {
        ModalBottomSheet(onDismissRequest = { creating = false; editing = null }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            val existing = editing
            var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
            var topic by rememberSaveable { mutableStateOf(existing?.topic ?: LearnTopic.SRHR) }
            var body by rememberSaveable { mutableStateOf(existing?.body ?: "") }
            var publish by rememberSaveable { mutableStateOf(existing?.published ?: true) }
            var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(if (existing == null) "New article" else "Edit article", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Field(title, { title = it; errors = errors - "title" }, "Title", error = errors["title"])
                FilterRow(LearnTopic.values().toList(), topic, { it.label }) { topic = it }
                Field(body, { body = it; errors = errors - "body" }, "Article", error = errors["body"], singleLine = false, minLines = 6)
                SwitchRow("Publish to users", publish) { publish = it }
                PillButton("Save", Modifier.fillMaxWidth()) {
                    errors = Validators.article(title, body)
                    if (errors.isEmpty()) {
                        val a = Article(existing?.id ?: "", title.trim(), topic, body.trim(), publish, existing?.author ?: "", existing?.date ?: vm.today())
                        if (vm.saveArticle(a) is Outcome.Done) { creating = false; editing = null }
                    }
                }
            }
        }
    }
    deleting?.let { a ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete this article?") },
            text = { Text("\"${a.title}\" will be removed for everyone.") },
            confirmButton = { TextButton(onClick = { vm.deleteArticle(a.id); deleting = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}
