package com.goldenboot.goldenbootfinancial.ui.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import goldenbootfinancial.composeapp.generated.resources.Res
import goldenbootfinancial.composeapp.generated.resources.app_icon_cd
import goldenbootfinancial.composeapp.generated.resources.ic_app_icon
import goldenbootfinancial.composeapp.generated.resources.sign_in_button
import goldenbootfinancial.composeapp.generated.resources.sign_in_demo
import goldenbootfinancial.composeapp.generated.resources.sign_in_opening
import goldenbootfinancial.composeapp.generated.resources.sign_in_subtitle
import goldenbootfinancial.composeapp.generated.resources.sign_in_title
import goldenbootfinancial.composeapp.generated.resources.sign_in_unavailable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val BrandYellow = Color(0xFFFFE600)
private val Ink = Color(0xFF111111)

/** Shown when the BFF needs a signed-in member. One implementation for every platform. */
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    SignInContent(
        state = state,
        canSignIn = viewModel.canSignIn,
        onSignIn = viewModel::signIn,
        onContinueInDemo = viewModel::continueInDemoMode,
        modifier = modifier
    )
}

@Composable
fun SignInContent(
    state: SignInUiState,
    canSignIn: Boolean,
    onSignIn: () -> Unit,
    onContinueInDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_app_icon),
                contentDescription = stringResource(Res.string.app_icon_cd),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = stringResource(Res.string.sign_in_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.sign_in_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            if (canSignIn) {
                val busy = state is SignInUiState.SigningIn
                Button(
                    onClick = onSignIn,
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Ink),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Ink)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(Res.string.sign_in_opening), fontWeight = FontWeight.Medium)
                    } else {
                        Text(stringResource(Res.string.sign_in_button), fontWeight = FontWeight.Medium)
                    }
                }
                if (state is SignInUiState.Error) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.sign_in_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                OutlinedButton(onClick = onContinueInDemo, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(Res.string.sign_in_demo))
                }
            }
        }
    }
}
