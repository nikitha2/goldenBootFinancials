package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import goldenbootfinancial.features.payroll.generated.resources.Res
import goldenbootfinancial.features.payroll.generated.resources.empty_title
import goldenbootfinancial.features.payroll.generated.resources.empty_subtitle
import goldenbootfinancial.features.payroll.generated.resources.error_title
import goldenbootfinancial.features.payroll.generated.resources.btn_retry
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model.PayrollItem

/**
 * Stateful Payroll screen.
 *
 * Owns the ViewModel and hoists state down to the stateless [PayrollContent].
 * Note: TopAppBar is managed by the parent App Scaffold.
 */
@Composable
fun PayrollScreen(
    modifier: Modifier = Modifier,
    viewModel: PayrollViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PayrollContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

/**
 * Stateless Payroll content.
 *
 * Pure UI that renders based on hoisted state. No ViewModel dependency,
 * making it easy to preview and test.
 */
@Composable
fun PayrollContent(
    uiState: PayrollUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is PayrollUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PayrollUiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(Res.string.empty_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is PayrollUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.items,
                    key = { item -> item.id }
                ) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (item.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }

        is PayrollUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(Res.string.error_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text(stringResource(Res.string.btn_retry))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PayrollContentLoadingPreview() {
    PayrollContent(
        uiState = PayrollUiState.Loading,
        onRefresh = {}
    )
}

@Preview
@Composable
private fun PayrollContentEmptyPreview() {
    PayrollContent(
        uiState = PayrollUiState.Empty,
        onRefresh = {}
    )
}

@Preview
@Composable
private fun PayrollContentSuccessPreview() {
    PayrollContent(
        uiState = PayrollUiState.Success(
            items = listOf(
                PayrollItem(id = "1", title = "Item 1", description = "Description 1"),
                PayrollItem(id = "2", title = "Item 2", description = "Description 2")
            )
        ),
        onRefresh = {}
    )
}
