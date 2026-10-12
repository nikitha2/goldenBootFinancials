# Sign-in (Clerk)

The app signs members in with Clerk's hosted sign-in page, opened in the system browser. That page
offers whatever the Clerk instance allows (Google) and runs the authenticator-code step and the
MFA enrollment itself, so the app does not rebuild any of it. The session token Clerk returns is
the same JWT the web app sends, so the backend accepts it unchanged.

## How it fits together

- `core/auth/SignInProvider` — the one interface the app uses (shared code).
- `core/auth/SessionManager` — holds the session, attaches the token to every request, and raises
  `signInRequired` when the BFF answers 401 or the member logs out.
- `ui/signin/SignInScreen` — the one sign-in screen, for every platform.
- `androidMain/ClerkSignInProvider` — the only Android-specific part: opens Clerk's page.
  Other platforms report "sign-in is not set up" until their provider exists.

`signInRequired` stays false while the BFF is happy without a token (mock mode), so local
development never bounces to the sign-in screen.

## Turn it on (Android)

1. In the Clerk Dashboard, open **Native applications** and enable the **Native API**.
2. Register the Android app (package names `com.goldenboot.goldenbootfinancial.test` and
   `.prod`) on that page.
3. Put the publishable key in `local.properties` (not committed) or `~/.gradle/gradle.properties`:

   ```
   clerk.publishableKey=pk_test_xxxxxxxx
   ```

4. Rebuild. Without a key the app still builds; the sign-in screen says it is not set up and offers
   "Continue in demo mode".

Members must already have MFA enrolled, or finish enrolment on Clerk's page. MFA is required for
every role (see `docs/beta-auth-roles.md` in the web repo).

## Not done yet

iOS, desktop and web have no sign-in provider. They show "not set up" and need their own
implementation of `SignInProvider`.
