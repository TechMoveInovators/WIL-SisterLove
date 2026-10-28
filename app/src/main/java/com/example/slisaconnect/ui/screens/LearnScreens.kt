package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*

@Composable
fun LearnScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var topic by rememberSaveable { mutableStateOf<LearnTopic?>(null) }
    val articles = state.visibleArticles().filter { it.published && (topic == null || it.topic == topic) }.sortedByDescending { it.date }
    ScreenFrame(title = "Learn", actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } }) {
        Muted("Learn & grow: SRHR, GBVF, HIV & PrEP")
        FilterRow(listOf<LearnTopic?>(null) + LearnTopic.values().toList(), topic, { it?.label ?: "All" }) { topic = it }
        if (articles.isEmpty()) EmptyState("Nothing published yet", "Articles appear here when the SLiSA team publishes them.")
        articles.forEach { a ->
            ListRow(a.title, "${a.topic.label} · ${a.date.pretty()}", onClick = { nav.to(Routes.article(a.id)) })
        }
        NoteBox("This is general information, not medical advice. For health questions visit your nearest clinic.")
    }
}

@Composable
fun ArticleScreen(id: String, nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val a = state.articles.firstOrNull { it.id == id && (it.published || state.can(Permission.LEARNING_MANAGE)) }
    ScreenFrame(title = "Learn", onBack = { nav.back() }) {
        if (a == null) { EmptyState("Not found", "This article was removed or is not published."); return@ScreenFrame }
        Panel {
            PlainChip(a.topic.label)
            Text(a.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Muted("${a.date.pretty()} · ${a.author}")
            Text(a.body)
        }
        if (a.topic == LearnTopic.GBVF || a.topic == LearnTopic.RIGHTS) {
            NoteBox("Need to talk to someone? The GBV Command Centre is free, 24 hours: 0800 428 428.", NoteKind.WARN)
            OutlinedButton(onClick = { ctx.dial("0800428428") }) { Text("Call 0800 428 428") }
        }
        TextButton(onClick = { ctx.shareText("${a.title}. Read more on SLiSA Connect.") }) { Text("Share") }
    }
}
