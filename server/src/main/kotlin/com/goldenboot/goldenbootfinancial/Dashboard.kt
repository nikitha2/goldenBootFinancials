package com.goldenboot.goldenbootfinancial

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

// =============================================================================
// What the app receives
// =============================================================================

/** Everything the Dashboard screen renders, already computed and formatted. */
@Serializable
data class DashboardSummary(
    val title: String,
    val subtitle: String,
    val notices: List<Notice> = emptyList(),
    /** Null once every setup step is done: a finished checklist is not shown. */
    val setup: SetupProgress? = null,
    val gamePlan: GamePlan,
    val netWorth: Metric,
    val wellnessScore: Metric,
    val savingsRate: Metric,
    val assets: Metric,
    val liabilities: Metric
)

/** A banner above the cards. [level] is "warning" or "error". */
@Serializable
data class Notice(val level: String, val message: String)

@Serializable
data class SetupProgress(val title: String, val percentComplete: Int, val steps: List<SetupStep>)

@Serializable
data class SetupStep(val id: String, val title: String, val done: Boolean)

/**
 * The black Game Plan card. [state] is "needs_profile", "ready" or "unavailable".
 * [allocation] is only filled when "ready".
 */
@Serializable
data class GamePlan(
    val eyebrow: String,
    val title: String,
    val state: String,
    val description: String,
    val ctaLabel: String? = null,
    val wellnessScore: Int? = null,
    val allocation: List<AllocationRow> = emptyList(),
    val footnote: String? = null
)

@Serializable
data class AllocationRow(val label: String, val percent: Int)

/** A headline-number card. Strings are display-ready ("$12,345", "12%"). */
@Serializable
data class Metric(
    val label: String,
    val value: String? = null,
    val suffix: String? = null,
    val hint: String? = null,
    val caption: String? = null,
    val delta: String? = null,
    val deltaPositive: Boolean = true
)

// =============================================================================
// Accounts (ported from the web Dashboard's account handling)
// =============================================================================

enum class AccountKind { HYSA, BROKERAGE, IRA, CD, CUSTODIAL, EMPLOYER_RETIREMENT, TRANSACTIONAL, CREDIT_CARD, LIABILITY, OTHER }

private val SavingsKinds = setOf(
    AccountKind.HYSA, AccountKind.BROKERAGE, AccountKind.IRA,
    AccountKind.CD, AccountKind.CUSTODIAL, AccountKind.EMPLOYER_RETIREMENT
)

private val KindLabels = mapOf(
    AccountKind.HYSA to "High-Yield Savings", AccountKind.BROKERAGE to "Brokerage", AccountKind.IRA to "IRA",
    AccountKind.CD to "CD", AccountKind.CUSTODIAL to "Custodial", AccountKind.EMPLOYER_RETIREMENT to "Employer Retirement",
    AccountKind.TRANSACTIONAL to "Transactional", AccountKind.CREDIT_CARD to "Credit Card",
    AccountKind.LIABILITY to "Liability", AccountKind.OTHER to "Other"
)

data class Account(
    val kind: AccountKind,
    val isLiability: Boolean,
    val balance: Double,
    val fromPlaid: Boolean
)

/** What the accounts call returned. A 404 means "no bank connected", which is normal for a new member. */
sealed interface AccountsOutcome {
    data class Loaded(val accounts: List<Account>, val failedConnections: List<String>) : AccountsOutcome
    data object NoneConnected : AccountsOutcome
    data object Failed : AccountsOutcome
}

internal fun normalizeKind(value: String?): AccountKind {
    val type = value.orEmpty().lowercase()
    return when {
        "transactional" in type || "depository" in type || "checking" in type || type == "savings" -> AccountKind.TRANSACTIONAL
        "credit_card" in type || "credit card" in type -> AccountKind.CREDIT_CARD
        "liability" in type || "loan" in type -> AccountKind.LIABILITY
        "custodial" in type || "529" in type || "utma" in type || "ugma" in type -> AccountKind.CUSTODIAL
        "employer" in type || "401" in type || "403" in type || "457" in type || "thrift savings" in type || type == "tsp" ->
            AccountKind.EMPLOYER_RETIREMENT
        "ira" in type || "roth" in type || "traditional" in type -> AccountKind.IRA
        "broker" in type || "investment" in type || "taxable" in type -> AccountKind.BROKERAGE
        "cd" in type || "certificate of deposit" in type -> AccountKind.CD
        "hysa" in type || "high yield" in type -> AccountKind.HYSA
        else -> AccountKind.OTHER
    }
}

/** Reads the backend's `{accounts:[...], errors:[...]}` from `GET /plaid/classified-accounts`. */
internal fun parseAccounts(response: JsonObject): AccountsOutcome.Loaded {
    val accounts = (response["accounts"] as? JsonArray).orEmpty().mapNotNull { element ->
        val raw = element as? JsonObject ?: return@mapNotNull null
        val kind = normalizeKind(
            raw.string("type") ?: raw.string("account_type") ?: raw.string("plaid_type")
                ?: raw.string("plaid_subtype") ?: raw.string("name")
        )
        val isLiability = raw.string("category")?.lowercase() == "liability" ||
            kind == AccountKind.CREDIT_CARD || kind == AccountKind.LIABILITY
        val balance = (raw.number("balance") ?: raw.number("current_balance") ?: raw.number("available_balance") ?: 0.0)
        Account(kind, isLiability, abs(balance).takeIf { it.isFinite() } ?: 0.0, fromPlaid = true)
    }
    val failed = (response["errors"] as? JsonArray).orEmpty().mapIndexedNotNull { index, entry ->
        val name = (entry as? JsonObject)?.string("institution_name")
        if (name.isNullOrBlank()) "Connection ${index + 1}" else name
    }
    return AccountsOutcome.Loaded(accounts, failed)
}

/**
 * Plaid accounts plus manual balances from the profile, for savings kinds with no connected account.
 * Never invents zero-balance rows.
 */
internal fun mergeAccounts(plaid: List<Account>, profile: JsonObject?): List<Account> {
    val connectedKinds = plaid.filter { !it.isLiability && it.kind in SavingsKinds }.map { it.kind }.toSet()
    val preferences = (profile?.get("accountPreferences") as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
    val manual = preferences.mapNotNull { preference ->
        val kind = runCatching { AccountKind.valueOf(preference.string("kind").orEmpty()) }.getOrNull()
            ?: return@mapNotNull null
        if (kind !in SavingsKinds || kind in connectedKinds) return@mapNotNull null
        val hasAccount = preference.string("hasAccount").orEmpty().lowercase()
        val action = preference.string("action").orEmpty().lowercase()
        val balance = abs(preference.number("balance") ?: 0.0)
        if (hasAccount != "yes" || action == "omit" || balance <= 0) return@mapNotNull null
        Account(kind, isLiability = false, balance = balance, fromPlaid = false)
    }
    return plaid + manual
}

/** Live balances the wellness rules may use. Sends nothing rather than 0 when a kind is not connected. */
internal fun connectedFinancials(plaid: List<Account>, highestApr: Double?): ConnectedFinancials {
    val assets = plaid.filter { !it.isLiability }
    fun sum(kinds: Set<AccountKind>) = assets.filter { it.kind in kinds }.sumOf { it.balance }
    val liquid = sum(setOf(AccountKind.HYSA))
    val investable = sum(SavingsKinds)
    val checking = sum(setOf(AccountKind.TRANSACTIONAL))
    val debt = plaid.filter { it.isLiability }.sumOf { it.balance }
    return ConnectedFinancials(
        liquidBalance = liquid.takeIf { it > 0 },
        investableBalance = investable.takeIf { it > 0 },
        checkingBalance = checking.takeIf { it > 0 },
        highestApr = highestApr,
        debtBalance = debt.takeIf { it > 0 }
    )
}

// =============================================================================
// History + formatting
// =============================================================================

data class Snapshot(val date: String, val value: Double)

/** Change versus roughly a month ago: newest minus the newest snapshot at least 28 days older, else the first. */
internal fun deltaVsLastMonth(history: List<Snapshot>): Pair<Double, Boolean>? {
    if (history.size < 2) return null
    val latest = history.last()
    val cutoff = LocalDate.parse(latest.date).minusDays(28)
    var previous = history.first()
    for (snapshot in history) if (!LocalDate.parse(snapshot.date).isAfter(cutoff)) previous = snapshot
    val sinceFirst = LocalDate.parse(previous.date).isAfter(cutoff)
    return (latest.value - previous.value) to sinceFirst
}

/** "$12,345", "-$1,234" — whole dollars, half rounded away from zero (matches Intl.NumberFormat). */
internal fun currency(value: Double): String {
    val v = if (value.isFinite()) value else 0.0
    val rounded = floor(abs(v) + 0.5).toLong()
    val sign = if (v < 0 && rounded > 0) "-" else ""
    return sign + "$" + String.format(Locale.US, "%,d", rounded)
}

private fun deltaText(delta: Pair<Double, Boolean>?, format: (Double) -> String): String? = delta?.let { (change, sinceFirst) ->
    "${if (change >= 0) "+" else "−"}${format(abs(change))} ${if (sinceFirst) "since first snapshot" else "vs last month"}"
}

private fun plural(count: Int, word: String) = "$count $word${if (count == 1) "" else "s"} tracked"

private fun firstName(profile: JsonObject?): String =
    ((profile?.get("household") as? JsonObject)?.string("fullName")).orEmpty().trim().substringBefore(' ')

private const val GAME_PLAN_FOOTNOTE =
    "This allocation applies to future savings, not your current balances. It describes what your " +
        "finances show; it is not a recommendation to buy, sell, or hold anything."

private val AllocationOrder = listOf(
    "High-Yield Savings" to "hysa", "Employer retirement" to "employerRetirement", "IRA" to "ira",
    "Brokerage" to "brokerage", "Custodial" to "custodial", "CD" to "cd"
)

// =============================================================================
// Assembly (pure: no I/O, easy to test)
// =============================================================================

/** Everything the upstream calls produced. Null / default means "unknown": hide a nudge, never invent one. */
data class DashboardInputs(
    val profile: JsonObject?,
    val questionnaireComplete: Boolean?,
    val hasContactDetails: Boolean?,
    val accounts: AccountsOutcome,
    val recommendation: RecommendationOutcome?,
    val netWorthHistory: List<Snapshot>,
    val wellnessHistory: List<Snapshot>
)

internal fun trackedAccounts(inputs: DashboardInputs): List<Account> {
    val plaid = (inputs.accounts as? AccountsOutcome.Loaded)?.accounts.orEmpty()
    return mergeAccounts(plaid, inputs.profile)
}

fun assembleDashboard(inputs: DashboardInputs): DashboardSummary {
    val tracked = trackedAccounts(inputs)
    val assets = tracked.filter { !it.isLiability }
    val liabilities = tracked.filter { it.isLiability }
    val totalAssets = assets.sumOf { it.balance }
    val totalLiabilities = liabilities.sumOf { it.balance }
    val netWorth = totalAssets - totalLiabilities
    val hasProfile = inputs.profile != null
    val hasConnectedAccount = tracked.any { it.fromPlaid }

    // ---- banners
    val notices = buildList {
        if (inputs.accounts is AccountsOutcome.Failed) {
            add(Notice("error", "Golden Boot could not load live Plaid accounts. Manual balances are still shown."))
        }
        val broken = (inputs.accounts as? AccountsOutcome.Loaded)?.failedConnections.orEmpty()
        if (broken.isNotEmpty()) {
            add(
                Notice(
                    "warning",
                    "${broken.joinToString(", ")} ${if (broken.size == 1) "needs" else "need"} attention — " +
                        "usually a quick re-login at the bank."
                )
            )
        }
    }

    // ---- setup checklist
    val steps = listOf(
        SetupStep("agreements", "Accept the agreements", true),
        SetupStep("billing", "Set up billing", true),
        SetupStep("questionnaire", "Answer the questionnaire", inputs.questionnaireComplete ?: true),
        SetupStep("contact", "Add your contact details", inputs.hasContactDetails ?: true),
        SetupStep("profile", "Build your financial profile", hasProfile),
        SetupStep("account", "Connect your first account", hasConnectedAccount)
    )
    val done = steps.count { it.done }
    val percent = (done * 100.0 / steps.size).roundToInt()
    val setup = if (done == steps.size) null else SetupProgress("Your setup is $percent% complete", percent, steps)

    // ---- game plan
    val recommendation = inputs.recommendation
    val ready = recommendation as? RecommendationOutcome.Ready
    val gamePlan = when {
        !hasProfile -> GamePlan(
            eyebrow = "GOLDEN BOOT COACH", title = "Game Plan", state = "needs_profile",
            description = "Complete the financial profile so Golden Boot can use your household, cash flow, goals, " +
                "risk preferences, debts, and six-account choices to generate a personalized savings strategy.",
            ctaLabel = "Complete financial profile"
        )
        ready != null -> GamePlan(
            eyebrow = "GOLDEN BOOT COACH", title = "Game Plan", state = "ready",
            description = "Top priority: ${ready.priority}",
            wellnessScore = ready.wellnessScore,
            allocation = AllocationOrder.map { (label, key) -> AllocationRow(label, ready.allocation[key] ?: 0) },
            footnote = "Rules-based${ready.ruleVersion?.let { " ($it)" }.orEmpty()}. $GAME_PLAN_FOOTNOTE"
        )
        else -> GamePlan(
            eyebrow = "GOLDEN BOOT COACH", title = "Game Plan", state = "unavailable",
            description = (recommendation as? RecommendationOutcome.Unavailable)?.message
                ?: "Golden Boot could not generate your Game Plan.",
            ctaLabel = "Review profile"
        )
    }

    // ---- metrics
    val netWorthDelta = deltaVsLastMonth(inputs.netWorthHistory)
    val wellnessDelta = deltaVsLastMonth(inputs.wellnessHistory)
    val score = ready?.wellnessScore
    val rate = ready?.monthlySavingsRate

    return DashboardSummary(
        title = firstName(inputs.profile).takeIf { it.isNotEmpty() }?.let { "$it’s Savings Dashboard" }
            ?: "Your Savings Dashboard",
        subtitle = netWorthDelta?.let { (change, sinceFirst) ->
            "Net worth ${if (change >= 0) "up" else "down"} ${currency(abs(change))} " +
                if (sinceFirst) "since your first snapshot" else "this month"
        } ?: "Your accounts, score, and Game Plan in one place",
        notices = notices,
        setup = setup,
        gamePlan = gamePlan,
        netWorth = Metric(
            label = "Net worth", value = currency(netWorth), hint = "Trend",
            caption = if (netWorthDelta == null) "Snapshots build a trend over time" else null,
            delta = deltaText(netWorthDelta, ::currency),
            deltaPositive = (netWorthDelta?.first ?: 0.0) >= 0
        ),
        wellnessScore = Metric(
            label = "Wellness score", value = score?.toString(), suffix = "/ 100",
            hint = "Why ${score ?: "—"}?",
            caption = if (wellnessDelta == null) "Out of 100, from fixed rules" else null,
            delta = deltaText(wellnessDelta) { "${floor(it + 0.5).toLong()} pts" },
            deltaPositive = (wellnessDelta?.first ?: 0.0) >= 0
        ),
        savingsRate = Metric(
            label = "Savings rate",
            value = rate?.let { if (it == floor(it)) "${it.toLong()}%" else "$it%" },
            caption = "Of monthly take-home pay"
        ),
        assets = Metric(label = "Assets", value = currency(totalAssets), caption = plural(assets.size, "account")),
        liabilities = Metric(
            label = "Liabilities", value = currency(totalLiabilities), caption = plural(liabilities.size, "account")
        )
    )
}

// =============================================================================
// Orchestration: the real backend calls
// =============================================================================

private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}

private fun Result<*>.upstreamStatus(): Int? = (exceptionOrNull() as? UpstreamException)?.status?.value

private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.number(key: String): Double? = (this[key] as? JsonPrimitive)?.doubleOrNull

private fun parseSnapshots(response: JsonElement): List<Snapshot> =
    ((response as? JsonObject)?.get("snapshots") as? JsonArray).orEmpty().mapNotNull {
        val snapshot = it as? JsonObject ?: return@mapNotNull null
        val date = snapshot.string("date") ?: return@mapNotNull null
        val value = snapshot.number("value") ?: return@mapNotNull null
        Snapshot(date, value)
    }

/**
 * Builds the Dashboard from the real backend, mirroring what the web Dashboard does:
 *
 *  1. profile, agreements, contact, accounts and liabilities, in parallel;
 *  2. the web app's rules engine for the wellness score, savings rate and Game Plan;
 *  3. today's net-worth and wellness readings, recorded and read back for the trend.
 *
 * Every piece except sign-in is optional: a failed call hides its card rather than failing the screen.
 * A 401 from the backend is thrown so the app can send the member back to sign-in.
 *
 * [today] is the member's local calendar day (the backend keys history by it).
 */
suspend fun buildDashboard(
    upstream: UpstreamClient,
    recommender: RecommendationClient,
    authorization: String?,
    requestId: String?,
    today: String
): DashboardSummary = coroutineScope {
    val profileCall = async { attempt { upstream.getJson("/profile", authorization, requestId) } }
    val legalCall = async { attempt { upstream.getJson("/legal/status", authorization, requestId) } }
    val contactCall = async { attempt { upstream.getJson("/legal/beta-contact", authorization, requestId) } }
    val accountsCall = async { attempt { upstream.getJson("/plaid/classified-accounts", authorization, requestId) } }
    val liabilitiesCall = async { attempt { upstream.getJson("/plaid/liabilities", authorization, requestId) } }

    val profileResult = profileCall.await()
    val legalResult = legalCall.await()
    val contactResult = contactCall.await()
    val accountsResult = accountsCall.await()
    val liabilitiesResult = liabilitiesCall.await()

    // Not signed in (or token rejected): nothing else here is meaningful.
    listOf(profileResult, legalResult, accountsResult).firstOrNull { it.upstreamStatus() == 401 }
        ?.let { throw it.exceptionOrNull()!! }

    val profile = profileResult.getOrNull()?.let { (it as? JsonObject)?.get("profile") as? JsonObject }
    val questionnaireComplete = (legalResult.getOrNull() as? JsonObject)?.get("questionnaire_complete")
        ?.let { (it as? JsonPrimitive)?.contentOrNull?.toBooleanStrictOrNull() }
    // Only a definite 404 means "no contact on file"; any other failure reads as complete.
    val hasContactDetails = if (contactResult.upstreamStatus() == 404) false else true

    val accountsOutcome: AccountsOutcome = when {
        accountsResult.isSuccess -> (accountsResult.getOrNull() as? JsonObject)?.let { parseAccounts(it) } ?: AccountsOutcome.Failed
        accountsResult.upstreamStatus() == 404 -> AccountsOutcome.NoneConnected
        else -> AccountsOutcome.Failed
    }
    val plaidAccounts = (accountsOutcome as? AccountsOutcome.Loaded)?.accounts.orEmpty()

    val highestApr = ((liabilitiesResult.getOrNull() as? JsonObject)?.get("debts") as? JsonArray).orEmpty()
        .mapNotNull { ((it as? JsonObject)?.number("apr")) }
        .filter { it.isFinite() }
        .maxOrNull()

    // The wellness score reads live balances, so this runs after the accounts are in.
    val recommendation = profile?.let { recommender.recommend(it, connectedFinancials(plaidAccounts, highestApr)) }

    val base = DashboardInputs(
        profile, questionnaireComplete, hasContactDetails, accountsOutcome, recommendation, emptyList(), emptyList()
    )
    val tracked = trackedAccounts(base)
    val netWorth = tracked.filter { !it.isLiability }.sumOf { it.balance } - tracked.filter { it.isLiability }.sumOf { it.balance }
    val ready = recommendation as? RecommendationOutcome.Ready

    val netWorthHistory = async {
        if (accountsOutcome is AccountsOutcome.Failed || tracked.isEmpty()) emptyList()
        else recordHistory(upstream, "net_worth", today, netWorth, null, authorization, requestId)
    }
    val wellnessHistory = async {
        if (ready == null) emptyList()
        else recordHistory(upstream, "wellness", today, ready.wellnessScore.toDouble(), ready.wellnessVersion, authorization, requestId)
    }

    assembleDashboard(base.copy(netWorthHistory = netWorthHistory.await().takeLast(12), wellnessHistory = wellnessHistory.await().takeLast(12)))
}

/** Records today's reading and returns the stored series; falls back to reading the series if the write fails. */
private suspend fun recordHistory(
    upstream: UpstreamClient,
    metric: String,
    today: String,
    value: Double,
    ruleVersion: String?,
    authorization: String?,
    requestId: String?
): List<Snapshot> {
    val body = buildJsonObject {
        put(
            "snapshots",
            buildJsonArray {
                add(
                    buildJsonObject {
                        put("date", today)
                        put("value", value)
                        ruleVersion?.let { put("rule_version", it) }
                    }
                )
            }
        )
    }
    attempt { upstream.postJson("/history/$metric", body, authorization, requestId) }.getOrNull()
        ?.let { return parseSnapshots(it) }
    return attempt { upstream.getJson("/history/$metric", authorization, requestId) }.getOrNull()
        ?.let(::parseSnapshots).orEmpty()
}

/** The member's local day from `?date=YYYY-MM-DD`, else today in UTC. */
fun resolveToday(requested: String?): String =
    requested?.takeIf { Regex("""\d{4}-\d{2}-\d{2}""").matches(it) && runCatching { LocalDate.parse(it) }.isSuccess }
        ?: LocalDate.now(ZoneOffset.UTC).toString()
