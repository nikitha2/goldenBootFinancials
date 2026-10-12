package com.goldenboot.goldenbootfinancial

import kotlinx.serialization.Serializable

/**
 * Generic item DTO returned by all mock endpoints. Matches the shape of the
 * scaffolder-generated client-side *Dto.kt files (id, title, description).
 */
@Serializable
data class Item(
    val id: String,
    val title: String,
    val description: String? = null
)

/**
 * Hardcoded mock data per feature module. Edit these lists or replace the
 * routes in Routing.kt with real implementations as the backend takes shape.
 */
object MockData {
    val dashboard = DashboardSummary(
        title = "Your Savings Dashboard",
        subtitle = "Your accounts, score, and Game Plan in one place",
        setup = SetupProgress(
            title = "Your setup is 50% complete",
            percentComplete = 50,
            steps = listOf(
                SetupStep("agreements", "Accept the agreements", true),
                SetupStep("billing", "Set up billing", true),
                SetupStep("questionnaire", "Answer the questionnaire", false),
                SetupStep("contact", "Add your contact details", true),
                SetupStep("profile", "Build your financial profile", false),
                SetupStep("account", "Connect your first account", false)
            )
        ),
        gamePlan = GamePlan(
            eyebrow = "GOLDEN BOOT COACH",
            title = "Game Plan",
            state = "needs_profile",
            description = "Complete the financial profile so Golden Boot can use your household, " +
                "cash flow, goals, risk preferences, debts, and six-account choices to generate " +
                "a personalized savings strategy.",
            ctaLabel = "Complete financial profile"
        ),
        netWorth = Metric(
            label = "Net worth", value = "\$0", hint = "Trend", caption = "Snapshots build a trend over time"
        ),
        wellnessScore = Metric(
            label = "Wellness score", value = null, suffix = "/ 100", hint = "Why —?",
            caption = "Out of 100, from fixed rules"
        ),
        savingsRate = Metric(label = "Savings rate", value = null, caption = "Of monthly take-home pay"),
        assets = Metric(label = "Assets", value = "\$0", caption = "0 accounts tracked"),
        liabilities = Metric(label = "Liabilities", value = "\$0", caption = "0 accounts tracked")
    )

    val cashflow = listOf(
        Item("c1", "Cashflow item 1", "Sample description for cashflow item 1"),
        Item("c2", "Cashflow item 2", "Sample description for cashflow item 2"),
        Item("c3", "Cashflow item 3", "Sample description for cashflow item 3")
    )

    val payroll = listOf(
        Item("p1", "Payroll item 1", "Sample description for payroll item 1"),
        Item("p2", "Payroll item 2", "Sample description for payroll item 2"),
        Item("p3", "Payroll item 3", "Sample description for payroll item 3")
    )

    val paycheckplan = listOf(
        Item("p1", "Paycheckplan item 1", "Sample description for paycheckplan item 1"),
        Item("p2", "Paycheckplan item 2", "Sample description for paycheckplan item 2"),
        Item("p3", "Paycheckplan item 3", "Sample description for paycheckplan item 3")
    )

    val investments = listOf(
        Item("i1", "Investments item 1", "Sample description for investments item 1"),
        Item("i2", "Investments item 2", "Sample description for investments item 2"),
        Item("i3", "Investments item 3", "Sample description for investments item 3")
    )

    val profile = listOf(
        Item("p1", "Profile item 1", "Sample description for profile item 1"),
        Item("p2", "Profile item 2", "Sample description for profile item 2"),
        Item("p3", "Profile item 3", "Sample description for profile item 3")
    )
}
