package com.goldenboot.goldenbootfinancial.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.jetbrains.compose.resources.stringResource
import com.goldenboot.goldenbootfinancial.core.auth.SessionManager
import com.goldenboot.goldenbootfinancial.ui.signin.SignInScreen
import com.goldenboot.goldenbootfinancial.ui.splash.SplashScreen
import org.koin.compose.koinInject
import goldenbootfinancial.composeapp.generated.resources.Res
import goldenbootfinancial.composeapp.generated.resources.tab_title_dashboard
import goldenbootfinancial.composeapp.generated.resources.tab_title_cashflow
import goldenbootfinancial.composeapp.generated.resources.tab_title_payroll
import goldenbootfinancial.composeapp.generated.resources.tab_title_paycheckplan
import goldenbootfinancial.composeapp.generated.resources.tab_title_investments
import goldenbootfinancial.composeapp.generated.resources.tab_title_profile
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.presentation.DashboardScreen
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.presentation.CashflowScreen
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.presentation.PayrollScreen
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.presentation.PaycheckplanScreen
import com.goldenboot.goldenbootfinancial.investments.feature.investments.presentation.InvestmentsScreen
import com.goldenboot.goldenbootfinancial.profile.feature.profile.presentation.ProfileScreen

/**
 * Navigation routes and host for the application.
 *
 * Single source of truth for all navigation destinations and route titles.
 * Bottom navigation items are built in App.kt using NavBarItem from core.
 */
object NavRoutes {
    const val Splash = "splash"
    const val SignIn = "signin"
    const val Dashboard = "dashboard"
    const val Cashflow = "cashflow"
    const val Payroll = "payroll"
    const val Paycheckplan = "paycheckplan"
    const val Investments = "investments"
    const val Profile = "profile"

    val tabRoutes = listOf(Dashboard, Cashflow, Payroll, Paycheckplan, Investments, Profile)
}

/**
 * Returns the display title for the given route from string resources.
 */
@Composable
fun currentTitle(route: String?): String {
    return when (route) {
        NavRoutes.Dashboard -> stringResource(Res.string.tab_title_dashboard)
        NavRoutes.Cashflow -> stringResource(Res.string.tab_title_cashflow)
        NavRoutes.Payroll -> stringResource(Res.string.tab_title_payroll)
        NavRoutes.Paycheckplan -> stringResource(Res.string.tab_title_paycheckplan)
        NavRoutes.Investments -> stringResource(Res.string.tab_title_investments)
        NavRoutes.Profile -> stringResource(Res.string.tab_title_profile)
        else -> ""
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val session: SessionManager = koinInject()
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(NavRoutes.Splash) {
            SplashScreen(
                onNavigateToHome = {
                    // The launch checks may already have learned the BFF wants a signed-in member.
                    val next = if (session.signInRequired.value) NavRoutes.SignIn else NavRoutes.Dashboard
                    navController.navigate(next) {
                        popUpTo(NavRoutes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.SignIn) { SignInScreen() }

        composable(NavRoutes.Dashboard) { DashboardScreen() }
        composable(NavRoutes.Cashflow) { CashflowScreen() }
        composable(NavRoutes.Payroll) { PayrollScreen() }
        composable(NavRoutes.Paycheckplan) { PaycheckplanScreen() }
        composable(NavRoutes.Investments) { InvestmentsScreen() }
        composable(NavRoutes.Profile) { ProfileScreen() }
    }
}
