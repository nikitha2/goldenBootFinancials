package com.goldenboot.goldenbootfinancial

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val parse = Json { ignoreUnknownKeys = true }

private fun profileWithHysa(balance: Int = 15000): JsonObject = parse.parseToJsonElement(
    """
    {"household":{"fullName":"Nikitha G"},"cashFlow":{},"foundation":{},"goals":[],"risk":{},
     "accountPreferences":[
       {"kind":"HYSA","hasAccount":"yes","action":"integrate","balance":$balance,"institution":"ally bank"},
       {"kind":"IRA","hasAccount":"no","action":"open"},
       {"kind":"CD","hasAccount":"yes","action":"omit","balance":900}
     ]}
    """.trimIndent()
).jsonObject

private val ready = RecommendationOutcome.Ready(
    wellnessScore = 53, wellnessVersion = "GBF-WELL-2.2", priority = "Pay down high-interest debt.",
    allocation = mapOf("hysa" to 45, "brokerage" to 27, "ira" to 28),
    ruleVersion = "GBF-ALLOC-1.1", monthlySavingsRate = 12.0
)

private fun inputs(
    profile: JsonObject? = null,
    accounts: AccountsOutcome = AccountsOutcome.NoneConnected,
    recommendation: RecommendationOutcome? = null,
    questionnaire: Boolean? = false,
    contact: Boolean? = true,
    netWorthHistory: List<Snapshot> = emptyList(),
    wellnessHistory: List<Snapshot> = emptyList()
) = DashboardInputs(profile, questionnaire, contact, accounts, recommendation, netWorthHistory, wellnessHistory)

class DashboardAssemblerTest {

    @Test
    fun newMemberSeesSetupAndAnEmptyDashboard() {
        val summary = assembleDashboard(inputs())

        assertEquals("Your Savings Dashboard", summary.title)
        val setup = assertNotNull(summary.setup)
        assertEquals(50, setup.percentComplete) // agreements, billing, contact done; 3 of 6
        assertEquals("Your setup is 50% complete", setup.title)
        assertEquals(listOf("questionnaire", "profile", "account"), setup.steps.filter { !it.done }.map { it.id })
        assertEquals("needs_profile", summary.gamePlan.state)
        assertEquals("Complete financial profile", summary.gamePlan.ctaLabel)
        assertEquals("$0", summary.netWorth.value)
        assertEquals("Snapshots build a trend over time", summary.netWorth.caption)
        assertNull(summary.wellnessScore.value)
        assertEquals("Why —?", summary.wellnessScore.hint)
        assertNull(summary.savingsRate.value)
        assertTrue(summary.notices.isEmpty())
    }

    @Test
    fun connectedMemberGetsTotalsScoreAndGamePlan() {
        val accounts = AccountsOutcome.Loaded(
            listOf(
                Account(AccountKind.HYSA, false, 15000.0, true),
                Account(AccountKind.TRANSACTIONAL, false, 2500.0, true),
                Account(AccountKind.CREDIT_CARD, true, 3000.0, true)
            ),
            failedConnections = emptyList()
        )
        val summary = assembleDashboard(inputs(profileWithHysa(), accounts, ready))

        assertEquals("Nikitha’s Savings Dashboard", summary.title)
        assertEquals("$17,500", summary.assets.value)
        assertEquals("2 accounts tracked", summary.assets.caption)
        assertEquals("$3,000", summary.liabilities.value)
        assertEquals("1 account tracked", summary.liabilities.caption)
        assertEquals("$14,500", summary.netWorth.value)
        assertEquals("53", summary.wellnessScore.value)
        assertEquals("Why 53?", summary.wellnessScore.hint)
        assertEquals("12%", summary.savingsRate.value)

        val plan = summary.gamePlan
        assertEquals("ready", plan.state)
        assertEquals("Top priority: Pay down high-interest debt.", plan.description)
        assertEquals(53, plan.wellnessScore)
        assertEquals(
            listOf("High-Yield Savings", "Employer retirement", "IRA", "Brokerage", "Custodial", "CD"),
            plan.allocation.map { it.label }
        )
        assertEquals(listOf(45, 0, 28, 27, 0, 0), plan.allocation.map { it.percent })
        assertContains(plan.footnote.orEmpty(), "Rules-based (GBF-ALLOC-1.1)")
        // profile + connected account done -> 5 of 6 (questionnaire still open)
        assertEquals(83, summary.setup?.percentComplete)
    }

    @Test
    fun finishedSetupHidesTheChecklist() {
        val accounts = AccountsOutcome.Loaded(listOf(Account(AccountKind.HYSA, false, 1.0, true)), emptyList())
        val summary = assembleDashboard(inputs(profileWithHysa(), accounts, ready, questionnaire = true))
        assertNull(summary.setup)
    }

    @Test
    fun unknownStatusNeverInventsAnUnfinishedStep() {
        val summary = assembleDashboard(inputs(questionnaire = null, contact = null))
        val open = summary.setup!!.steps.filter { !it.done }.map { it.id }
        assertEquals(listOf("profile", "account"), open)
    }

    @Test
    fun manualBalancesFillKindsThatHaveNoConnectedAccount() {
        // HYSA 15,000 counted (not connected, yes, integrate); IRA has no balance; CD is omitted.
        val summary = assembleDashboard(inputs(profileWithHysa(), AccountsOutcome.NoneConnected, ready))
        assertEquals("$15,000", summary.assets.value)
        assertEquals("1 account tracked", summary.assets.caption)

        // Once Plaid reports an HYSA, the manual one is not double counted.
        val connected = AccountsOutcome.Loaded(listOf(Account(AccountKind.HYSA, false, 20000.0, true)), emptyList())
        assertEquals("$20,000", assembleDashboard(inputs(profileWithHysa(), connected, ready)).assets.value)
    }

    @Test
    fun trendTextUsesTheMonthAgoSnapshot() {
        val history = listOf(Snapshot("2026-09-01", 10000.0), Snapshot("2026-09-20", 11000.0), Snapshot("2026-10-09", 12500.0))
        val summary = assembleDashboard(inputs(netWorthHistory = history))

        assertEquals("+$2,500 vs last month", summary.netWorth.delta)
        assertEquals("Net worth up $2,500 this month", summary.subtitle)
        assertNull(summary.netWorth.caption)

        val shortHistory = listOf(Snapshot("2026-10-01", 5000.0), Snapshot("2026-10-09", 4000.0))
        val down = assembleDashboard(inputs(netWorthHistory = shortHistory))
        assertEquals("−$1,000 since first snapshot", down.netWorth.delta)
        assertEquals(false, down.netWorth.deltaPositive)
        assertEquals("Net worth down $1,000 since your first snapshot", down.subtitle)
    }

    @Test
    fun wellnessTrendIsShownInPoints() {
        // Newest reading is more than 28 days after the first -> compared with "last month".
        val month = listOf(Snapshot("2026-09-01", 50.0), Snapshot("2026-10-09", 56.4))
        assertEquals("+6 pts vs last month", assembleDashboard(inputs(wellnessHistory = month)).wellnessScore.delta)

        // Younger history is compared with the very first reading.
        val young = listOf(Snapshot("2026-10-01", 60.0), Snapshot("2026-10-09", 57.0))
        val summary = assembleDashboard(inputs(wellnessHistory = young))
        assertEquals("−3 pts since first snapshot", summary.wellnessScore.delta)
        assertEquals(false, summary.wellnessScore.deltaPositive)
    }

    @Test
    fun accountProblemsBecomeBanners() {
        val failed = assembleDashboard(inputs(accounts = AccountsOutcome.Failed))
        assertEquals("error", failed.notices.single().level)

        val broken = AccountsOutcome.Loaded(emptyList(), listOf("Chase", "Connection 2"))
        val warning = assembleDashboard(inputs(accounts = broken)).notices.single()
        assertEquals("warning", warning.level)
        assertContains(warning.message, "Chase, Connection 2 need attention")
    }

    @Test
    fun rulesEngineFailureIsShownOnTheGamePlanCard() {
        val summary = assembleDashboard(
            inputs(profileWithHysa(), recommendation = RecommendationOutcome.Unavailable("Finish the required questions."))
        )
        assertEquals("unavailable", summary.gamePlan.state)
        assertEquals("Finish the required questions.", summary.gamePlan.description)
        assertEquals("Review profile", summary.gamePlan.ctaLabel)
    }

    @Test
    fun formatsCurrencyLikeTheWebApp() {
        assertEquals("$0", currency(0.0))
        assertEquals("$1,235", currency(1234.5))
        assertEquals("-$1,235", currency(-1234.5))
        assertEquals("$0", currency(-0.2))
        assertEquals("$1,000,000", currency(1_000_000.0))
        assertEquals("$0", currency(Double.NaN))
    }

    @Test
    fun classifiesAccountKinds() {
        assertEquals(AccountKind.HYSA, normalizeKind("HYSA"))
        assertEquals(AccountKind.TRANSACTIONAL, normalizeKind("TRANSACTIONAL"))
        assertEquals(AccountKind.EMPLOYER_RETIREMENT, normalizeKind("EMPLOYER_RETIREMENT"))
        assertEquals(AccountKind.CUSTODIAL, normalizeKind("CUSTODIAL"))
        assertEquals(AccountKind.IRA, normalizeKind("IRA"))
        assertEquals(AccountKind.CREDIT_CARD, normalizeKind("CREDIT_CARD"))
        assertEquals(AccountKind.LIABILITY, normalizeKind("LIABILITY"))
        assertEquals(AccountKind.BROKERAGE, normalizeKind("BROKERAGE"))
        assertEquals(AccountKind.OTHER, normalizeKind(null))
    }

    @Test
    fun connectedFinancialsOmitsMissingKindsInsteadOfSendingZero() {
        val plaid = listOf(Account(AccountKind.HYSA, false, 100.0, true))
        val json = connectedFinancials(plaid, highestApr = null).toJson()
        assertEquals(setOf("liquidBalance", "investableBalance"), json.keys)
    }

    @Test
    fun resolvesTheMemberDay() {
        assertEquals("2026-10-09", resolveToday("2026-10-09"))
        assertTrue(resolveToday("not-a-date").matches(Regex("""\d{4}-\d{2}-\d{2}""")))
        assertTrue(resolveToday("2026-13-45").matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }
}

class DashboardProxyTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    /** A fake FastAPI backend + a fake web-app rules route. */
    private class Fakes(val hasProfile: Boolean = true, val accountsStatus: HttpStatusCode = HttpStatusCode.OK) {
        val seen: MutableList<String> = java.util.concurrent.CopyOnWriteArrayList()
        var authSeen: String? = null
        var historyBody: String? = null
        var recommendBody: String? = null
        private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        val upstream = UpstreamClient("http://backend", HttpClient(MockEngine { req ->
            seen += "${req.method.value} ${req.url.encodedPath}"
            authSeen = req.headers[HttpHeaders.Authorization]
            when (req.url.encodedPath) {
                "/profile" ->
                    if (hasProfile) respond(
                        """{"profile":${profileWithHysa()},"version":2}""", HttpStatusCode.OK, jsonHeaders
                    ) else respond("""{"detail":"none"}""", HttpStatusCode.NotFound, jsonHeaders)
                "/legal/status" -> respond(
                    """{"all_documents_accepted":true,"intake_complete":true,"questionnaire_complete":true,"beta_onboarding_complete":true}""",
                    HttpStatusCode.OK, jsonHeaders
                )
                "/legal/beta-contact" -> respond("""{"detail":"none"}""", HttpStatusCode.NotFound, jsonHeaders)
                "/plaid/classified-accounts" ->
                    if (accountsStatus.value in 200..299) respond(
                        """{"accounts":[
                             {"id":"a1","name":"Savings","type":"HYSA","category":"asset","balance":15000.0},
                             {"id":"a2","name":"Checking","type":"TRANSACTIONAL","category":"asset","balance":2500.0},
                             {"id":"a3","name":"Visa","type":"CREDIT_CARD","category":"liability","balance":3000.0}
                           ],"errors":[{"institution_name":"Chase"}]}""",
                        HttpStatusCode.OK, jsonHeaders
                    ) else respond("""{"detail":"x"}""", accountsStatus, jsonHeaders)
                "/plaid/liabilities" -> respond(
                    """{"debts":[{"apr":24.9},{"apr":19.0},{"apr":null}]}""", HttpStatusCode.OK, jsonHeaders
                )
                "/history/net_worth", "/history/wellness" -> {
                    if (req.method == HttpMethod.Post) historyBody = req.body.toByteArray().decodeToString()
                    respond(
                        """{"metric":"x","written":1,"snapshots":[
                             {"date":"2026-09-01","value":10000.0},{"date":"2026-10-09","value":14500.0}]}""",
                        HttpStatusCode.OK, jsonHeaders
                    )
                }
                else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
            }
        }) { install(ContentNegotiation) { json() } })

        val recommender = RecommendationClient("http://web", HttpClient(MockEngine { req ->
            recommendBody = req.body.toByteArray().decodeToString()
            respond(
                """{"wellnessScore":53,"wellness":{"version":"GBF-WELL-2.2"},"priority":"Pay down debt.",
                    "accountAllocation":{"hysa":45,"brokerage":27,"ira":28,"cd":0,"custodial":0,"employerRetirement":0},
                    "ruleVersion":"GBF-ALLOC-1.1","metrics":{"monthlySavingsRate":12}}""",
                HttpStatusCode.OK, jsonHeaders
            )
        }) { install(ContentNegotiation) { json() } })
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.app(fakes: Fakes, config: BffConfig) =
        application {
            configureSerialization()
            configureRouting(config, fakes.upstream, MockStore(), fakes.recommender)
        }

    @Test
    fun composesTheDashboardFromTheRealBackendCalls() = testApplication {
        val fakes = Fakes()
        app(fakes, BffConfig(mode = "proxy"))

        val response = client.get("/dashboard?date=2026-10-09") { header(HttpHeaders.Authorization, "Bearer abc") }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        assertEquals("Bearer abc", fakes.authSeen)
        listOf(
            "GET /profile", "GET /legal/status", "GET /legal/beta-contact", "GET /plaid/classified-accounts",
            "GET /plaid/liabilities", "POST /history/net_worth", "POST /history/wellness"
        ).forEach { assertTrue(it in fakes.seen, "$it was not called: ${fakes.seen}") }

        assertEquals("Nikitha’s Savings Dashboard", body["title"]!!.jsonPrimitive.content)
        assertEquals("$14,500", body["netWorth"]!!.jsonObject["value"]!!.jsonPrimitive.content)
        assertEquals("+$4,500 vs last month", body["netWorth"]!!.jsonObject["delta"]!!.jsonPrimitive.content)
        assertEquals("53", body["wellnessScore"]!!.jsonObject["value"]!!.jsonPrimitive.content)
        assertEquals("12%", body["savingsRate"]!!.jsonObject["value"]!!.jsonPrimitive.content)
        assertEquals("ready", body["gamePlan"]!!.jsonObject["state"]!!.jsonPrimitive.content)
        assertEquals(6, body["gamePlan"]!!.jsonObject["allocation"]!!.jsonArray.size)
        assertEquals("warning", body["notices"]!!.jsonArray.single().jsonObject["level"]!!.jsonPrimitive.content)

        // Missing contact (404) leaves that step open; questionnaire + profile + account are done.
        val open = body["setup"]!!.jsonObject["steps"]!!.jsonArray
            .filter { !it.jsonObject["done"]!!.jsonPrimitive.content.toBoolean() }
            .map { it.jsonObject["id"]!!.jsonPrimitive.content }
        assertEquals(listOf("contact"), open)
    }

    @Test
    fun sendsLiveFiguresToTheRulesEngineWithoutInventingZeros() = testApplication {
        val fakes = Fakes()
        app(fakes, BffConfig(mode = "proxy"))
        client.get("/dashboard") { header(HttpHeaders.Authorization, "Bearer abc") }

        val sent = Json.parseToJsonElement(fakes.recommendBody!!).jsonObject
        assertTrue("profile" in sent)
        val connected = sent["connected"]!!.jsonObject
        assertEquals("15000.0", connected["liquidBalance"]!!.jsonPrimitive.content)
        assertEquals("15000.0", connected["investableBalance"]!!.jsonPrimitive.content)
        assertEquals("2500.0", connected["checkingBalance"]!!.jsonPrimitive.content)
        assertEquals("24.9", connected["highestApr"]!!.jsonPrimitive.content)
        assertEquals("3000.0", connected["debtBalance"]!!.jsonPrimitive.content)
    }

    @Test
    fun recordsTodaysReadingForTheMembersLocalDay() = testApplication {
        val fakes = Fakes()
        app(fakes, BffConfig(mode = "proxy"))
        client.get("/dashboard?date=2026-10-09") { header(HttpHeaders.Authorization, "Bearer abc") }

        val posted = Json.parseToJsonElement(fakes.historyBody!!).jsonObject["snapshots"]!!.jsonArray.single().jsonObject
        assertEquals("2026-10-09", posted["date"]!!.jsonPrimitive.content)
    }

    @Test
    fun aMemberWithoutAProfileGetsTheNeedsProfileCardAndNoRulesCall() = testApplication {
        val fakes = Fakes(hasProfile = false)
        app(fakes, BffConfig(mode = "proxy"))
        val body = Json.parseToJsonElement(
            client.get("/dashboard") { header(HttpHeaders.Authorization, "Bearer abc") }.bodyAsText()
        ).jsonObject

        assertEquals("needs_profile", body["gamePlan"]!!.jsonObject["state"]!!.jsonPrimitive.content)
        assertNull(fakes.recommendBody)
        assertTrue("POST /history/wellness" !in fakes.seen)
    }

    @Test
    fun noBankConnectedIsNormalNotAnError() = testApplication {
        val fakes = Fakes(accountsStatus = HttpStatusCode.NotFound)
        app(fakes, BffConfig(mode = "proxy"))
        val body = Json.parseToJsonElement(
            client.get("/dashboard") { header(HttpHeaders.Authorization, "Bearer abc") }.bodyAsText()
        ).jsonObject
        assertEquals(0, body["notices"]!!.jsonArray.size)
    }

    @Test
    fun aRejectedTokenIsRelayedAs401() = testApplication {
        val fakes = Fakes(accountsStatus = HttpStatusCode.Unauthorized)
        app(fakes, BffConfig(mode = "proxy"))
        val response = client.get("/dashboard") { header(HttpHeaders.Authorization, "Bearer bad") }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun requiresSignInUnlessAnonymousIsAllowed() = testApplication {
        val fakes = Fakes()
        app(fakes, BffConfig(mode = "proxy"))
        assertEquals(HttpStatusCode.Unauthorized, client.get("/dashboard").status)
        assertTrue(fakes.seen.isEmpty())
    }
}
