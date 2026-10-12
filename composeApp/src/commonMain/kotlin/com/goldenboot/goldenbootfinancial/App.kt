package com.goldenboot.goldenbootfinancial

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import org.koin.compose.koinInject
import androidx.compose.ui.graphics.painter.Painter
import com.goldenboot.goldenbootfinancial.core.ui.drawer.DrawerItem
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.goldenboot.goldenbootfinancial.core.ui.bottombar.BottomNavigationBar
import com.goldenboot.goldenbootfinancial.core.ui.bottombar.NavBarItem
import com.goldenboot.goldenbootfinancial.ui.navigation.AppNavHost
import com.goldenboot.goldenbootfinancial.ui.navigation.NavRoutes
import com.goldenboot.goldenbootfinancial.ui.navigation.currentTitle
import com.goldenboot.goldenbootfinancial.core.auth.SessionManager
import com.goldenboot.goldenbootfinancial.core.config.AppInfo
import com.goldenboot.goldenbootfinancial.core.ui.drawer.AppDrawerContent
import com.goldenboot.goldenbootfinancial.core.theme.AppBarStyling
import com.goldenboot.goldenbootfinancial.core.theme.GoldenbootTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalFocusManager
import goldenbootfinancial.composeapp.generated.resources.Res
import goldenbootfinancial.composeapp.generated.resources.tab_title_dashboard
import goldenbootfinancial.composeapp.generated.resources.tab_title_cashflow
import goldenbootfinancial.composeapp.generated.resources.tab_title_payroll
import goldenbootfinancial.composeapp.generated.resources.tab_title_paycheckplan
import goldenbootfinancial.composeapp.generated.resources.tab_title_investments
import goldenbootfinancial.composeapp.generated.resources.tab_title_profile
import goldenbootfinancial.composeapp.generated.resources.ic_tab_dashboard
import goldenbootfinancial.composeapp.generated.resources.ic_tab_cashflow
import goldenbootfinancial.composeapp.generated.resources.ic_tab_payroll
import goldenbootfinancial.composeapp.generated.resources.ic_tab_paycheckplan
import goldenbootfinancial.composeapp.generated.resources.ic_tab_investments
import goldenbootfinancial.composeapp.generated.resources.ic_tab_profile
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.goldenboot.goldenbootfinancial.core.ui.topbar.GoldenbootTopAppBar
import goldenbootfinancial.composeapp.generated.resources.drawer_menu_title
import goldenbootfinancial.composeapp.generated.resources.drawer_logout
import goldenbootfinancial.composeapp.generated.resources.ic_logout
import goldenbootfinancial.composeapp.generated.resources.drawer_label_dashboard
import goldenbootfinancial.composeapp.generated.resources.drawer_label_cashflow
import goldenbootfinancial.composeapp.generated.resources.drawer_label_payroll
import goldenbootfinancial.composeapp.generated.resources.drawer_label_paycheckplan
import goldenbootfinancial.composeapp.generated.resources.drawer_label_investments
import goldenbootfinancial.composeapp.generated.resources.drawer_label_profile

/**
 * Main App composable.
 *
 * Entry point of the application that sets up:
 * - Single NavHost for all navigation (splash + tabs)
 * - Scaffold with conditional TopAppBar and BottomNavigationBar
 * - GoldenbootTheme (design tokens)
 *
 * Note: Koin DI is initialized in platform-specific entry points
 * (MainApplication on Android, MainViewController on iOS).
 */
@Composable
fun App() {
    val appInfo: AppInfo = koinInject()
    val session: SessionManager = koinInject()
    GoldenbootTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val isTabScreen = currentRoute in NavRoutes.tabRoutes
        val currentTitle = currentTitle(currentRoute)
        var isSearchVisible by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        val navBarItems = listOf(
            NavBarItem(
                route = NavRoutes.Dashboard,
                titleRes = Res.string.tab_title_dashboard,
                iconPainter = { painterResource(Res.drawable.ic_tab_dashboard) }
            ),
            NavBarItem(
                route = NavRoutes.Cashflow,
                titleRes = Res.string.tab_title_cashflow,
                iconPainter = { painterResource(Res.drawable.ic_tab_cashflow) }
            ),
            NavBarItem(
                route = NavRoutes.Payroll,
                titleRes = Res.string.tab_title_payroll,
                iconPainter = { painterResource(Res.drawable.ic_tab_payroll) }
            ),
            NavBarItem(
                route = NavRoutes.Paycheckplan,
                titleRes = Res.string.tab_title_paycheckplan,
                iconPainter = { painterResource(Res.drawable.ic_tab_paycheckplan) }
            ),
            NavBarItem(
                route = NavRoutes.Investments,
                titleRes = Res.string.tab_title_investments,
                iconPainter = { painterResource(Res.drawable.ic_tab_investments) }
            ),
            NavBarItem(
                route = NavRoutes.Profile,
                titleRes = Res.string.tab_title_profile,
                iconPainter = { painterResource(Res.drawable.ic_tab_profile) }
            )
        )
        val drawerItems = listOf(
            DrawerItem(
                stringResource(Res.string.drawer_label_dashboard), NavRoutes.Dashboard,
                stringResource(Res.string.tab_title_dashboard)
            ) { painterResource(Res.drawable.ic_tab_dashboard) },
            DrawerItem(
                stringResource(Res.string.drawer_label_cashflow), NavRoutes.Cashflow,
                stringResource(Res.string.tab_title_cashflow)
            ) { painterResource(Res.drawable.ic_tab_cashflow) },
            DrawerItem(
                stringResource(Res.string.drawer_label_payroll), NavRoutes.Payroll,
                stringResource(Res.string.tab_title_payroll)
            ) { painterResource(Res.drawable.ic_tab_payroll) },
            DrawerItem(
                stringResource(Res.string.drawer_label_paycheckplan), NavRoutes.Paycheckplan,
                stringResource(Res.string.tab_title_paycheckplan)
            ) { painterResource(Res.drawable.ic_tab_paycheckplan) },
            DrawerItem(
                stringResource(Res.string.drawer_label_investments), NavRoutes.Investments,
                stringResource(Res.string.tab_title_investments)
            ) { painterResource(Res.drawable.ic_tab_investments) },
            DrawerItem(
                stringResource(Res.string.drawer_label_profile), NavRoutes.Profile,
                stringResource(Res.string.tab_title_profile)
            ) { painterResource(Res.drawable.ic_tab_profile) }
        )
        val logoutIcon: @Composable () -> Painter = { painterResource(Res.drawable.ic_logout) }
        var isDrawerExpanded by rememberSaveable { mutableStateOf(true) }

        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // Sign-in is shown whenever the BFF rejects us or the member logs out, and the app moves on
        // as soon as that clears (successful sign-in, or "continue in demo mode").
        val signInRequired by session.signInRequired.collectAsState()
        LaunchedEffect(signInRequired, currentRoute) {
            val onStartup = currentRoute == null || currentRoute == NavRoutes.Splash
            if (signInRequired && currentRoute != NavRoutes.SignIn && !onStartup) {
                navController.navigate(NavRoutes.SignIn) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            } else if (!signInRequired && currentRoute == NavRoutes.SignIn) {
                navController.navigate(NavRoutes.Dashboard) {
                    popUpTo(NavRoutes.SignIn) { inclusive = true }
                }
            }
        }
        val logout: () -> Unit = { scope.launch { session.signOut() } }

        val navigateTo: (String) -> Unit = { route ->
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        val topBar: @Composable (Boolean) -> Unit = { showMenuIcon ->
            if (isTabScreen) {
                GoldenbootTopAppBar(
                    title = currentTitle,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    isSearchVisible = isSearchVisible,
                    onSearchToggle = {
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible) searchQuery = ""
                    },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchSubmit = { query ->
                        // TODO: handle search query
                    },
                    showMenuIcon = showMenuIcon
                )
            }
        }

        // Screen content, centered with a readable max width on wide windows.
        val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxSize()) {
                    AppNavHost(
                        navController = navController,
                        paddingValues = PaddingValues()
                    )
                }
            }
        }

        // Re-evaluated whenever the window is resized, so desktop/web adapt live.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val showPermanentDrawer = maxWidth >= ExpandedMinWidth && isTabScreen

            if (showPermanentDrawer) {
                // Wide window: always-visible side drawer, no bottom bar, no hamburger.
                PermanentNavigationDrawer(
                    drawerContent = {
                        AppDrawerContent(
                            title = stringResource(Res.string.drawer_menu_title),
                            items = drawerItems,
                            currentRoute = currentRoute,
                            onItemClick = navigateTo,
                            logoutLabel = stringResource(Res.string.drawer_logout),
                            onLogoutClick = logout,
                            versionText = appInfo.displayText,
                            permanent = true,
                            expanded = isDrawerExpanded,
                            onToggleExpanded = { isDrawerExpanded = !isDrawerExpanded },
                            logoutIcon = logoutIcon
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = { topBar(false) }
                    ) { paddingValues -> content(paddingValues) }
                }
            } else {
                // Narrow window: modal drawer + bottom navigation bar.
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = isTabScreen,
                    drawerContent = {
                        AppDrawerContent(
                            title = stringResource(Res.string.drawer_menu_title),
                            items = drawerItems,
                            currentRoute = currentRoute,
                            onItemClick = { route ->
                                scope.launch { drawerState.close() }
                                navigateTo(route)
                            },
                            logoutLabel = stringResource(Res.string.drawer_logout),
                            onLogoutClick = {
                                scope.launch { drawerState.close() }
                                logout()
                            },
                            versionText = appInfo.displayText,
                            logoutIcon = logoutIcon
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = { topBar(true) },
                        bottomBar = {
                            if (isTabScreen) {
                                BottomNavigationBar(
                                    items = navBarItems,
                                    currentRoute = currentRoute,
                                    onNavigate = navigateTo
                                )
                            }
                        }
                    ) { paddingValues -> content(paddingValues) }
                }
            }
        }
    }
}

/** Window width at or above which the permanent side drawer replaces the bottom bar. */
private val ExpandedMinWidth = 840.dp

/** Widest the screen content grows on large windows. */
private val ContentMaxWidth = 960.dp
