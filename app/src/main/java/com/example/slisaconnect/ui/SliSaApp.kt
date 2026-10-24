package com.example.slisaconnect.ui

package za.org.slisa.connect.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import za.org.slisa.connect.model.AdminArea
import za.org.slisa.connect.model.canOpen
import za.org.slisa.connect.ui.screens.*

object Routes {
    const val HOME = "home"
    const val EVENTS = "events"
    const val VOLUNTEER = "volunteer"
    const val LEARN = "learn"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "profile/edit"
    const val TEAMS = "teams"
    const val TEAM_REGISTER = "teams/register"
    const val TEAM_DASHBOARD = "teams/{teamId}"
    fun team(id: String) = "teams/$id"
    const val ADMIN = "admin"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_TEAMS = "admin/teams"
    const val ADMIN_EVENTS = "admin/events"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Home", Icons.Default.Home),
    Tab(Routes.EVENTS, "Events", Icons.Default.DateRange),
    Tab(Routes.VOLUNTEER, "Volunteer", Icons.Default.Favorite),
    Tab(Routes.LEARN, "Learn", Icons.Default.Star),
    Tab(Routes.PROFILE, "Profile", Icons.Default.Person),
)

@Composable
fun SlisaApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabRoutes = tabs.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    // Admin pages and Edit profile are reached from Profile, so Profile stays highlighted.
                    val selected = if (route in tabRoutes) route == tab.route else tab.route == Routes.PROFILE
                    NavigationBarItem(
                        selected = selected,
                        onClick = { nav.openTab(tab.route) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(padding)) {
            composable(Routes.HOME) {
                HomeScreen(onOpenAdmin = { nav.navigate(Routes.ADMIN) }, onOpenYouth = { nav.navigate(Routes.TEAMS) })
            }
            composable(Routes.EVENTS) { PlaceholderScreen("Events") }
            composable(Routes.VOLUNTEER) { PlaceholderScreen("Volunteer") }
            composable(Routes.LEARN) { PlaceholderScreen("Learn") }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onEdit = { nav.navigate(Routes.EDIT_PROFILE) },
                    onOpenAdmin = { nav.navigate(Routes.ADMIN) },
                    onOpenTeams = { nav.navigate(Routes.TEAMS) },
                    onOpenTeam = { nav.navigate(Routes.team(it)) },
                )
            }

            // Team registration. Open to every signed-in user; each team is private to its manager and coaches.
            composable(Routes.TEAMS) {
                TeamHubScreen(
                    onBack = { nav.popBackStack() },
                    onRegister = { nav.navigate(Routes.TEAM_REGISTER) },
                    onOpen = { nav.navigate(Routes.team(it)) },
                )
            }
            composable(Routes.TEAM_REGISTER) {
                RegisterTeamScreen(
                    onBack = { nav.popBackStack() },
                    onRegistered = { id -> nav.navigate(Routes.team(id)) { popUpTo(Routes.TEAM_REGISTER) { inclusive = true } } },
                )
            }
            composable(Routes.TEAM_DASHBOARD, arguments = listOf(navArgument("teamId") { type = NavType.StringType })) { entry ->
                TeamDashboardScreen(teamId = entry.arguments?.getString("teamId").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.EDIT_PROFILE) { EditProfileScreen(onDone = { nav.popBackStack() }) }

            // Protected admin routes. Each one checks the signed-in user's permission before showing anything.
            composable(Routes.ADMIN) {
                Guarded(AdminArea.DASHBOARD, nav) {
                    AdminDashboardScreen(
                        onBack = { nav.popBackStack() },
                        onUsers = { nav.navigate(Routes.ADMIN_USERS) },
                        onTeams = { nav.navigate(Routes.ADMIN_TEAMS) },
                        onEvents = { nav.navigate(Routes.ADMIN_EVENTS) },
                    )
                }
            }
            composable(Routes.ADMIN_USERS) {
                Guarded(AdminArea.USERS, nav) { UserManagementScreen(onBack = { nav.popBackStack() }) }
            }
            composable(Routes.ADMIN_TEAMS) {
                Guarded(AdminArea.TEAMS, nav) { TeamApprovalScreen(onBack = { nav.popBackStack() }) }
            }
            composable(Routes.ADMIN_EVENTS) {
                Guarded(AdminArea.EVENTS, nav) { EventManagementScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}

private fun NavHostController.openTab(route: String) = navigate(route) {
    popUpTo(Routes.HOME) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/**
 * Route guard for every admin screen.
 * - Allowed: show the screen.
 * - Not staff at all: send the user to their profile without any message, so the admin area is never mentioned.
 * - Staff without this particular permission: say the page is not part of their role.
 * It runs on every recomposition, so switching role while on an admin page also takes effect straight away.
 */
@Composable
private fun Guarded(area: AdminArea, nav: NavHostController, vm: AppViewModel = viewModel(), content: @Composable () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    when {
        state.canOpen(area) -> content()
        !state.currentUser.role.isStaff -> LaunchedEffect(Unit) { nav.openTab(Routes.PROFILE) }
        else -> ScreenFrame(title = "Not available", onBack = { nav.popBackStack() }) {
            AccessDenied(state.currentUser.role, onBack = { nav.popBackStack() })
        }
    }
}
