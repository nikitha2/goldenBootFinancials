package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import goldenbootfinancial.features.dashboard.generated.resources.Res
import goldenbootfinancial.features.dashboard.generated.resources.error_title
import goldenbootfinancial.features.dashboard.generated.resources.btn_retry
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.AllocationRow
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.DashboardSummary
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.GamePlan
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.Metric
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.Notice
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.SetupProgress
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.SetupStep

/**
 * Stateful Dashboard screen.
 *
 * Owns the ViewModel and hoists state down to the stateless [DashboardContent].
 * Note: TopAppBar is managed by the parent App Scaffold.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

/**
 * Stateless Dashboard content.
 *
 * Pure UI that renders based on hoisted state. No ViewModel dependency,
 * making it easy to preview and test.
 */
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is DashboardUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DashboardUiState.Success -> DashboardSummaryContent(uiState.summary, modifier)

        is DashboardUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

// -----------------------------------------------------------------------------
// Cards (match the Golden Boot web dashboard)
// -----------------------------------------------------------------------------

private val BrandYellow = Color(0xFFFFE600)
private val Ink = Color(0xFF111111)
private val DoneGreen = Color(0xFF0F7B4B)
private val Negative = Color(0xFFD64545)
private val Warning = Color(0xFF8A6D00)
private val CardShape = RoundedCornerShape(2.dp)

/** Card palette that follows light / dark without touching the app theme. */
private data class DashboardColors(
    val onPage: Color,
    val card: Color,
    val tile: Color,
    val muted: Color,
    val track: Color,
    val pending: Color
)

@Composable
private fun dashboardColors(): DashboardColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        DashboardColors(
            onPage = Color(0xFFF2F2F0),
            card = Color(0xFF1F1F1E),
            tile = Color(0xFF2A2A28),
            muted = Color(0xFFA8A8A4),
            track = Color(0xFF3A3A38),
            pending = Color(0xFF4A4A48)
        )
    } else {
        DashboardColors(
            onPage = Ink,
            card = Color(0xFFF3F3F2),
            tile = Color.White,
            muted = Color(0xFF6B6B68),
            track = Color(0xFFE3E3E1),
            pending = Color(0xFFDADAD7)
        )
    }
}

@Composable
private fun DashboardSummaryContent(summary: DashboardSummary, modifier: Modifier = Modifier) {
    val colors = dashboardColors()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DashboardHeader(summary.title, summary.subtitle, colors) }
        items(summary.notices) { NoticeBanner(it, colors) }
        summary.setup?.let { setup -> item { SetupCard(setup, colors) } }
        item { GamePlanCard(summary.gamePlan) }
        item { MetricCard(summary.netWorth, colors, valueSize = 44.sp, modifier = Modifier.fillMaxWidth()) }
        item { MetricPair(summary.wellnessScore, summary.savingsRate, colors) }
        item {
            MetricPair(
                summary.assets, summary.liabilities, colors,
                firstValueColor = DoneGreen, secondValueColor = Negative
            )
        }
    }
}

/** Two cards side by side on wide windows, stacked on phones. */
@Composable
private fun MetricPair(
    first: Metric,
    second: Metric,
    colors: DashboardColors,
    firstValueColor: Color? = null,
    secondValueColor: Color? = null
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(first, colors, valueColor = firstValueColor, modifier = Modifier.weight(1f))
                MetricCard(second, colors, valueColor = secondValueColor, modifier = Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(first, colors, valueColor = firstValueColor, modifier = Modifier.fillMaxWidth())
                MetricCard(second, colors, valueColor = secondValueColor, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun NoticeBanner(notice: Notice, colors: DashboardColors) {
    val tint = if (notice.level == Notice.Level.Error) Negative else Warning
    Text(
        text = notice.message,
        style = MaterialTheme.typography.bodyMedium,
        color = tint,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, tint.copy(alpha = 0.5f), CardShape)
            .background(colors.card, CardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DashboardHeader(title: String, subtitle: String, colors: DashboardColors) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = colors.onPage
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.muted
        )
    }
}

@Composable
private fun SetupCard(setup: SetupProgress, colors: DashboardColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(colors.card)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = setup.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = colors.onPage
        )
        ProgressBar(setup.fraction, colors)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = when {
                maxWidth >= 640.dp -> 3
                maxWidth >= 420.dp -> 2
                else -> 1
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                setup.steps.chunked(columns).forEach { rowSteps ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowSteps.forEach { step ->
                            StepTile(step, colors, Modifier.weight(1f))
                        }
                        // Keep tiles the same width on a short last row.
                        repeat(columns - rowSteps.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float, colors: DashboardColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CardShape)
            .background(colors.track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(BrandYellow)
        )
    }
}

@Composable
private fun StepTile(step: SetupStep, colors: DashboardColors, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CardShape)
            .background(colors.tile)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusDot(done = step.done, colors = colors)
        Text(
            text = step.title,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onPage
        )
    }
}

@Composable
private fun StatusDot(done: Boolean, colors: DashboardColors) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (done) DoneGreen else colors.pending),
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            // Drawn, not a "✓" glyph: the web (Wasm) default font has no check mark.
            Canvas(modifier = Modifier.size(12.dp)) {
                val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val check = Path().apply {
                    moveTo(size.width * 0.12f, size.height * 0.55f)
                    lineTo(size.width * 0.40f, size.height * 0.82f)
                    lineTo(size.width * 0.90f, size.height * 0.20f)
                }
                drawPath(check, color = Color.White, style = stroke)
            }
        }
    }
}

@Composable
private fun GamePlanCard(plan: GamePlan) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Ink)
            .then(if (dark) Modifier.border(1.dp, Color(0x33FFFFFF), CardShape) else Modifier)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = plan.eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.5.sp,
                    color = BrandYellow
                )
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            plan.wellnessScore?.let { WellnessBadge(it) }
        }
        Text(
            text = plan.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.widthIn(max = 640.dp).padding(top = 8.dp)
        )
        if (plan.allocation.isNotEmpty()) AllocationBars(plan.allocation)
        plan.footnote?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        plan.ctaLabel?.let { label ->
            val primary = plan.state == GamePlan.State.NeedsProfile
            Button(
                onClick = { /* TODO: open the financial profile flow */ },
                shape = CardShape,
                colors = if (primary) {
                    ButtonDefaults.buttonColors(containerColor = BrandYellow, contentColor = Ink)
                } else {
                    ButtonDefaults.outlinedButtonColors(contentColor = BrandYellow)
                },
                border = if (primary) null else androidx.compose.foundation.BorderStroke(1.dp, BrandYellow),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(label, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun WellnessBadge(score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(1.dp, BrandYellow, CardShape)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text("WELLNESS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(
            text = score.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandYellow
        )
    }
}

@Composable
private fun AllocationBars(rows: List<AllocationRow>) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Recommended future-savings allocation",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandYellow
        )
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = row.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.width(150.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(row.percent.coerceIn(0, 100) / 100f)
                            .fillMaxHeight()
                            .background(BrandYellow)
                    )
                }
                Text(
                    text = "${row.percent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(44.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    metric: Metric,
    colors: DashboardColors,
    modifier: Modifier = Modifier,
    valueSize: androidx.compose.ui.unit.TextUnit = 36.sp,
    valueColor: Color? = null
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(colors.card)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.muted,
                modifier = Modifier.weight(1f)
            )
            metric.hint?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = metric.value ?: "—",
                fontSize = valueSize,
                fontWeight = FontWeight.Medium,
                color = valueColor ?: colors.onPage
            )
            metric.suffix?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
        metric.delta?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (metric.deltaPositive) DoneGreen else Negative
            )
        }
        metric.caption?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge, color = colors.muted)
        }
    }
}

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

private val previewSummary = DashboardSummary(
    title = "Your Savings Dashboard",
    subtitle = "Your accounts, score, and Game Plan in one place",
    notices = listOf(Notice(Notice.Level.Warning, "Chase needs attention — usually a quick re-login at the bank.")),
    setup = SetupProgress(
        title = "Your setup is 50% complete",
        percentComplete = 50,
        steps = listOf(
            SetupStep("a", "Accept the agreements", true),
            SetupStep("b", "Set up billing", true),
            SetupStep("c", "Answer the questionnaire", false)
        )
    ),
    gamePlan = GamePlan(
        "GOLDEN BOOT COACH", "Game Plan", GamePlan.State.Ready, "Top priority: Pay down debt.", null, 53,
        listOf(AllocationRow("High-Yield Savings", 45), AllocationRow("Brokerage", 27), AllocationRow("IRA", 28)),
        "Rules-based (GBF-ALLOC-1.1)."
    ),
    netWorth = Metric("Net worth", "$14,500", null, "Trend", null, "+$4,500 vs last month", true),
    wellnessScore = Metric("Wellness score", "53", "/ 100", "Why 53?", "Out of 100, from fixed rules", null, true),
    savingsRate = Metric("Savings rate", "12%", null, null, "Of monthly take-home pay", null, true),
    assets = Metric("Assets", "$17,500", null, null, "2 accounts tracked", null, true),
    liabilities = Metric("Liabilities", "$3,000", null, null, "1 account tracked", null, true)
)

@Preview
@Composable
private fun DashboardContentLoadingPreview() {
    DashboardContent(uiState = DashboardUiState.Loading, onRefresh = {})
}

@Preview
@Composable
private fun DashboardContentSuccessPreview() {
    DashboardContent(uiState = DashboardUiState.Success(previewSummary), onRefresh = {})
}
