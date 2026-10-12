package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.dto

import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.AllocationRow
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.DashboardSummary
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.GamePlan
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.Metric
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.Notice
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.SetupProgress
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.SetupStep
import kotlinx.serialization.Serializable

/** Network shape of the BFF's /dashboard response. */
@Serializable
data class DashboardSummaryDto(
    val title: String,
    val subtitle: String,
    val notices: List<NoticeDto> = emptyList(),
    val setup: SetupProgressDto? = null,
    val gamePlan: GamePlanDto,
    val netWorth: MetricDto,
    val wellnessScore: MetricDto,
    val savingsRate: MetricDto,
    val assets: MetricDto,
    val liabilities: MetricDto
) {
    fun toDomain() = DashboardSummary(
        title = title,
        subtitle = subtitle,
        notices = notices.map { it.toDomain() },
        setup = setup?.toDomain(),
        gamePlan = gamePlan.toDomain(),
        netWorth = netWorth.toDomain(),
        wellnessScore = wellnessScore.toDomain(),
        savingsRate = savingsRate.toDomain(),
        assets = assets.toDomain(),
        liabilities = liabilities.toDomain()
    )
}

@Serializable
data class NoticeDto(val level: String, val message: String) {
    fun toDomain() = Notice(
        level = if (level == "error") Notice.Level.Error else Notice.Level.Warning,
        message = message
    )
}

@Serializable
data class SetupProgressDto(val title: String, val percentComplete: Int, val steps: List<SetupStepDto>) {
    fun toDomain() = SetupProgress(title, percentComplete, steps.map { it.toDomain() })
}

@Serializable
data class SetupStepDto(val id: String, val title: String, val done: Boolean) {
    fun toDomain() = SetupStep(id, title, done)
}

@Serializable
data class GamePlanDto(
    val eyebrow: String,
    val title: String,
    val state: String,
    val description: String,
    val ctaLabel: String? = null,
    val wellnessScore: Int? = null,
    val allocation: List<AllocationRowDto> = emptyList(),
    val footnote: String? = null
) {
    fun toDomain() = GamePlan(
        eyebrow = eyebrow,
        title = title,
        state = when (state) {
            "ready" -> GamePlan.State.Ready
            "unavailable" -> GamePlan.State.Unavailable
            else -> GamePlan.State.NeedsProfile
        },
        description = description,
        ctaLabel = ctaLabel,
        wellnessScore = wellnessScore,
        allocation = allocation.map { AllocationRow(it.label, it.percent) },
        footnote = footnote
    )
}

@Serializable
data class AllocationRowDto(val label: String, val percent: Int)

@Serializable
data class MetricDto(
    val label: String,
    val value: String? = null,
    val suffix: String? = null,
    val hint: String? = null,
    val caption: String? = null,
    val delta: String? = null,
    val deltaPositive: Boolean = true
) {
    fun toDomain() = Metric(label, value, suffix, hint, caption, delta, deltaPositive)
}
