package com.goldenboot.goldenbootfinancial.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import goldenbootfinancial.composeapp.generated.resources.Res
import goldenbootfinancial.composeapp.generated.resources.app_icon_cd
import goldenbootfinancial.composeapp.generated.resources.app_name
import goldenbootfinancial.composeapp.generated.resources.ic_app_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stateful splash screen displayed at app launch.
 *
 * Owns the ViewModel and hoists state down to the stateless [SplashContent].
 * Automatically navigates to the home screen after initialization.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel()
) {
    val isReady by viewModel.isReady.collectAsState()

    LaunchedEffect(isReady) {
        if (isReady) {
            onNavigateToHome()
        }
    }

    SplashContent(modifier = modifier)
}

/**
 * Stateless splash content.
 *
 * Pure UI that shows app branding with a simple fade-in animation.
 * No ViewModel dependency, making it easy to preview and test.
 */
@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    // Simple fade-in animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDE84F)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            // App logo
            Image(
                painter = painterResource(Res.drawable.ic_app_icon),
                contentDescription = stringResource(Res.string.app_icon_cd),
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // App name
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview
@Composable
private fun SplashContentPreview() {
    SplashContent()
}
