package com.goldenboot.goldenbootfinancial.core.navigation

import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages navigation state and actions across the application.
 *
 * This class provides a centralized way to handle navigation,
 * making it easier to navigate from ViewModels and business logic.
 */
class NavigationManager {
    private val _navController = MutableStateFlow<NavHostController?>(null)
    val navController: StateFlow<NavHostController?> = _navController.asStateFlow()

    fun setNavController(controller: NavHostController) {
        _navController.value = controller
    }

    fun navigate(route: String) {
        _navController.value?.navigate(route)
    }

    fun navigateBack() {
        _navController.value?.popBackStack()
    }

    fun navigateAndClearStack(route: String) {
        _navController.value?.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }
}
