package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model

/** Everything the Dashboard screen renders; the BFF has already computed and formatted it. */
data class DashboardSummary(
    val title: String,
    val subtitle: String,
    val notices: List<Notice>,
    /** Null once every setup step is done. */
    val setup: SetupProgress?,
    val gamePlan: GamePlan,
    val netWorth: Metric,
    val wellnessScore: Metric,
    val savingsRate: Metric,
    val assets: Metric,
    val liabilities: Metric
)

data class Notice(val level: Level, val message: String) {
    enum class Level { Warning, Error }
}

data class SetupProgress(val title: String, val percentComplete: Int, val steps: List<SetupStep>) {
    /** 0f..1f for the progress bar. */
    val fraction: Float get() = percentComplete.coerceIn(0, 100) / 100f
}

data class SetupStep(val id: String, val title: String, val done: Boolean)

data class GamePlan(
    val eyebrow: String,
    val title: String,
    val state: State,
    val description: String,
    val ctaLabel: String?,
    val wellnessScore: Int?,
    val allocation: List<AllocationRow>,
    val footnote: String?
) {
    enum class State { NeedsProfile, Ready, Unavailable }
}

data class AllocationRow(val label: String, val percent: Int)

/** A headline-number card. [value] is null until the member has data. */
data class Metric(
    val label: String,
    val value: String?,
    val suffix: String?,
    val hint: String?,
    val caption: String?,
    val delta: String?,
    val deltaPositive: Boolean
)
