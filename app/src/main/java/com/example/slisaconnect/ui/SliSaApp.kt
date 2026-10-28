package com.example.slisaconnect.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.screens.*

/** Every screen in the app. The first group is before sign-in, then the user side, then the staff-only admin side. */
object Routes {
    // Before sign-in
    const val WELCOME = "welcome"
    const val SIGN_IN = "signin"
    const val SIGN_UP = "signup"

    // User side
    const val HOME = "home"
    const val EVENTS = "events"
    const val EVENT = "event/{id}"
    const val VOLUNTEER = "volunteer"
    const val LEARN = "learn"
    const val ARTICLE = "article/{id}"
    const val YOUTH = "youth"
    const val TEAM_REGISTER = "youth/register"
    const val TEAM = "team/{id}"
    const val WOMEN = "women"
    const val SUPPORT = "support"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "profile/edit"
    const val SECURITY = "profile/security"
    const val HELP = "profile/help"
    const val MY_EVENTS = "profile/events"
    const val NOTIFICATIONS = "notifications"

    // Admin side (staff only)
    const val ADMIN = "admin"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_TEAMS = "admin/teams"
    const val ADMIN_EVENTS = "admin/events"
    const val ADMIN_SUPPORT = "admin/support"
    const val ADMIN_LEARNING = "admin/learning"
    const val ADMIN_REPORTS = "admin/reports"
    const val ADMIN_ALERTS = "admin/alerts"
    const val ADMIN_ACCOUNT = "admin/account"

    fun event(id: String) = "event/$id"
    fun article(id: String) = "article/$id"
    fun team(id: String) = "team/$id"

    fun isAdmin(route: String?) = route != null && (route == ADMIN || route.startsWith("admin/"))
}

/** Where tapping a message in the inbox goes. Unknown links go nowhere. */
fun routeFor(link: String?): String? = when {
    link == null -> null
    link.startsWith("team:") -> Routes.team(link.removePrefix("team:"))
    link.startsWith("event:") -> Routes.event(link.removePrefix("event:"))
    link.startsWith("article:") -> Routes.article(link.removePrefix("article:"))
    link == Links.ADMIN_TEAMS -> Routes.ADMIN_TEAMS
    link == Links.ADMIN_EVENTS -> Routes.ADMIN_EVENTS
    link == Links.ADMIN_SUPPORT -> Routes.ADMIN_SUPPORT
    link == Links.ADMIN_LEARNING -> Routes.ADMIN_LEARNING
    else -> null
}

/** A thin wrapper so screens ask for "go to" and "back" and never touch the navigation library directly. */
class Navigator(private val controller: NavHostController) {
    fun to(route: String) = controller.navigate(route)

    fun back() { controller.popBackStack() }

    /** Switches between the main sections, keeping each section's place. */
    fun tab(route: String) = controller.navigate(route) {
        popUpTo(controller.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    /** Opens [route] and removes [removing] from the back stack, for example after a form is submitted. */
    fun replace(route: String, removing: String) = controller.navigate(route) {
        popUpTo(removing) { inclusive = true }
    }

    fun open(link: String?) {
        val route = routeFor(link) ?: return
        controller.navigate(route)
    }
}

private data class BarItem(val route: String, val label: String, val icon: ImageVector, val badge: Int = 0)

@Composable
fun SlisaApp(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    val user = state.currentUser
    // A new navigation state for every sign-in and sign-out, so nobody inherits the last person's screens.
    key(user?.id) {
        val controller = rememberNavController()
        val nav = remember(controller) { Navigator(controller) }
        val route = controller.currentBackStackEntryAsState().value?.destination?.route

        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (user != null && route != null) {
                    if (Routes.isAdmin(route)) AdminBar(nav, route, state) else UserBar(nav, route, state)
                }
            },
        ) { padding ->
            val start = when {
                user == null -> Routes.WELCOME
                user.role.isStaff -> Routes.ADMIN
                else -> Routes.HOME
            }
            NavHost(controller, startDestination = start, modifier = Modifier.padding(padding)) {
                composable(Routes.WELCOME) { WelcomeScreen(nav) }
                composable(Routes.SIGN_IN) { SignInScreen(nav) }
                composable(Routes.SIGN_UP) { SignUpScreen(nav) }

                // User side. Everyone signed in can open these.
                composable(Routes.HOME) { DashboardScreen(nav) }
                composable(Routes.EVENTS) { EventsScreen(nav) }
                composable(Routes.EVENT) { e -> EventDetailScreen(e.id(), nav) }
                composable(Routes.VOLUNTEER) { VolunteerScreen(nav) }
                composable(Routes.LEARN) { LearnScreen(nav) }
                composable(Routes.ARTICLE) { e -> ArticleScreen(e.id(), nav) }
                composable(Routes.YOUTH) { YouthBaseScreen(nav) }
                composable(Routes.TEAM_REGISTER) { RegisterTeamScreen(nav) }
                composable(Routes.TEAM) { e -> TeamDashboardScreen(e.id(), nav) }
                composable(Routes.WOMEN) { WomenBaseScreen(nav) }
                composable(Routes.SUPPORT) { SupportScreen(nav) }
                composable(Routes.PROFILE) { ProfileScreen(nav) }
                composable(Routes.EDIT_PROFILE) { EditProfileScreen(nav) }
                composable(Routes.SECURITY) { PrivacySecurityScreen(nav) }
                composable(Routes.HELP) { HelpScreen(nav) }
                composable(Routes.MY_EVENTS) { MyEventsScreen(nav) }
                composable(Routes.NOTIFICATIONS) { NotificationsScreen(nav, staffView = false) }

                // Admin side. Each route checks the signed-in person's permission before it shows anything.
                composable(Routes.ADMIN) { Guarded(AdminArea.DASHBOARD, nav) { AdminDashboardScreen(nav) } }
                composable(Routes.ADMIN_USERS) { Guarded(AdminArea.USERS, nav) { UserManagementScreen(nav) } }
                composable(Routes.ADMIN_TEAMS) { Guarded(AdminArea.TEAMS, nav) { TeamApprovalScreen(nav) } }
                composable(Routes.ADMIN_EVENTS) { Guarded(AdminArea.EVENTS, nav) { EventManagementScreen(nav) } }
                composable(Routes.ADMIN_SUPPORT) { Guarded(AdminArea.SUPPORT, nav) { SupportManagementScreen(nav) } }
                composable(Routes.ADMIN_LEARNING) { Guarded(AdminArea.LEARNING, nav) { LearningManagementScreen(nav) } }
                composable(Routes.ADMIN_REPORTS) { Guarded(AdminArea.DASHBOARD, nav) { ReportsScreen(nav) } }
                composable(Routes.ADMIN_ALERTS) { Guarded(AdminArea.DASHBOARD, nav) { NotificationsScreen(nav, staffView = true) } }
                composable(Routes.ADMIN_ACCOUNT) { Guarded(AdminArea.DASHBOARD, nav) { AdminAccountScreen(nav) } }
            }
        }
    }
}

private fun NavBackStackEntry.id(): String = arguments?.getString("id").orEmpty()

/**
 * Route guard for every admin screen.
 * - Allowed: show the screen.
 * - Not staff at all: send the person to their home without any message, so the admin side is never mentioned.
 * - Staff without this particular permission: say the page is not part of their role.
 * It runs on every recomposition, so a role change takes effect straight away.
 */
@Composable
private fun Guarded(area: AdminArea, nav: Navigator, vm: AppViewModel = viewModel(), content: @Composable () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val user = state.currentUser
    when {
        user == null -> Box {}
        state.canOpen(area) -> content()
        !user.role.isStaff -> LaunchedEffect(Unit) { nav.tab(Routes.HOME) }
        else -> ScreenFrame(title = "Not available", onBack = { nav.back() }) {
            AccessDenied(user.role, onBack = { nav.back() })
        }
    }
}

// ---- bottom bars --------------------------------------------------------------------------------

private fun userTab(route: String): String = when {
    route == Routes.EVENTS || route == Routes.EVENT -> Routes.EVENTS
    route == Routes.VOLUNTEER -> Routes.VOLUNTEER
    route == Routes.LEARN || route == Routes.ARTICLE -> Routes.LEARN
    route.startsWith("profile") || route == Routes.NOTIFICATIONS -> Routes.PROFILE
    else -> Routes.HOME
}

private fun adminTab(route: String): String = when (route) {
    Routes.ADMIN_TEAMS, Routes.ADMIN_EVENTS, Routes.ADMIN_ALERTS, Routes.ADMIN_ACCOUNT -> route
    else -> Routes.ADMIN
}

@Composable
private fun UserBar(nav: Navigator, route: String, state: AppState) {
    val items = listOf(
        BarItem(Routes.HOME, "Home", Icons.Default.Home),
        BarItem(Routes.EVENTS, "Events", Icons.Default.DateRange),
        BarItem(Routes.VOLUNTEER, "Volunteer", Icons.Default.Favorite),
        BarItem(Routes.LEARN, "Learn", Icons.Default.Star),
        BarItem(Routes.PROFILE, "Profile", Icons.Default.Person, badge = state.unreadCount),
    )
    Bar(items, selected = userTab(route), nav = nav)
}

@Composable
private fun AdminBar(nav: Navigator, route: String, state: AppState) {
    // Staff only see the sections their role can use.
    val items = buildList {
        add(BarItem(Routes.ADMIN, "Overview", Icons.Default.Home))
        if (state.can(Permission.TEAMS_REVIEW)) add(BarItem(Routes.ADMIN_TEAMS, "Teams", Icons.Default.Star))
        if (state.can(Permission.EVENTS_MANAGE)) add(BarItem(Routes.ADMIN_EVENTS, "Events", Icons.Default.DateRange))
        add(BarItem(Routes.ADMIN_ALERTS, "Alerts", Icons.Default.Notifications, badge = state.unreadCount))
        add(BarItem(Routes.ADMIN_ACCOUNT, "Account", Icons.Default.Person))
    }
    Bar(items, selected = adminTab(route), nav = nav)
}

@Composable
private fun Bar(items: List<BarItem>, selected: String, nav: Navigator) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.route == selected,
                onClick = { nav.tab(item.route) },
                icon = {
                    BadgedBox(badge = { if (item.badge > 0) Badge { Text(if (item.badge > 9) "9+" else "${item.badge}") } }) {
                        Icon(item.icon, contentDescription = null)
                    }
                },
                label = { Text(item.label) },
            )
        }
    }
}
