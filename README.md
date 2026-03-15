# Offline Splitwise

Offline Splitwise is an Android app for tracking shared expenses inside groups, recording settlements, and calculating who owes whom with a clean offline-first workflow.

The app is built with Kotlin, Jetpack Compose, and Room, and is designed to work fully offline with local persistence.

## What the app does

This app is a local alternative to Splitwise for small groups such as:

- trips
- roommates
- friends
- events
- shared household expenses

Inside the app, a user can:

- create multiple groups
- add members using only a name
- add expenses with one or more payers
- split an expense equally or assign an exact amount per member
- record settlements when one person pays another back
- see each member's total paid amount, owed amount, and net balance
- switch to a simplified debt view that reduces the number of transfers

## How the workflow works

1. Create a group.
2. Add members to that group.
3. Add an expense.
4. Choose who paid and how much each payer contributed.
5. Choose how the expense should be split:
   - equal split
   - exact amount per member
6. Save the expense.
7. Repeat with more expenses over time.
8. Open the balances screen to see the current net result.
9. Enable `Simplify` to see the minimum set of suggested transfers.
10. When someone actually pays another person back, record a settlement so balances update correctly.

## Example

If:

- A paid 800 in total
- B paid 600 in total
- C paid 0

and each member also has their own share of the expenses, the app calculates each member's net balance:

- positive balance = this member should receive money
- negative balance = this member should pay money
- zero balance = settled

Then the `Simplify` mode matches debtors and creditors and shows the smallest useful set of transfers, such as:

- `C pays A`
- `B pays A`

instead of showing a noisy ledger-style list of partial obligations.

## Features

- fully offline and local-first
- multiple independent groups
- member creation with name only
- create, edit, and delete expenses
- create, edit, and delete settlements
- multiple payers per expense
- equal split support
- exact split support
- balance calculation per member
- simplified debt suggestions
- Persian and English UI
- automatic RTL/LTR layout switching based on language
- light and dark theme support
- grouped amount formatting with thousands separators
- splash screen and custom launcher icon

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- ViewModel
- StateFlow
- Navigation Compose
- KSP

## Project structure

The project is split into three main layers:

- `data`
  - Room database
  - DAO layer
  - repositories
  - UI settings persistence
- `domain`
  - domain models
  - use cases
  - balance and simplify logic
- `ui`
  - screens
  - view models
  - theme
  - localization

Main files:

- [MainActivity.kt](app/src/main/java/com/encer/offlinesplitwise/MainActivity.kt)
- [App.kt](app/src/main/java/com/encer/offlinesplitwise/ui/App.kt)
- [Screens.kt](app/src/main/java/com/encer/offlinesplitwise/ui/Screens.kt)
- [ViewModels.kt](app/src/main/java/com/encer/offlinesplitwise/ui/ViewModels.kt)
- [UseCases.kt](app/src/main/java/com/encer/offlinesplitwise/domain/UseCases.kt)
- [OfflineSplitwiseDatabase.kt](app/src/main/java/com/encer/offlinesplitwise/data/local/OfflineSplitwiseDatabase.kt)
- [UserPreferences.kt](app/src/main/java/com/encer/offlinesplitwise/data/preferences/UserPreferences.kt)
- [Theme.kt](app/src/main/java/com/encer/offlinesplitwise/ui/theme/Theme.kt)

## Balance calculation

For each member:

- `paidTotal = sum of expense payments + incoming settlements`
- `owedTotal = sum of expense shares + outgoing settlements`
- `netBalance = paidTotal - owedTotal`

Interpretation:

- `netBalance > 0` means the member should receive money
- `netBalance < 0` means the member owes money
- `netBalance = 0` means the member is settled

## Simplify algorithm

The simplify mode works like this:

1. Build a list of debtors from negative balances.
2. Build a list of creditors from positive balances.
3. Match the largest debtor with the largest creditor.
4. Transfer `min(abs(debtor), creditor)`.
5. Update both sides.
6. Continue until all balances reach zero.

This produces a short, practical transfer list without changing the original ledger data.

## Running the project

Requirements:

- Android Studio
- Android SDK 36
- JDK 11

Run locally:

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Useful commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew testDebugUnitTest
./gradlew :app:assembleRelease
```

## Release build

Release optimizations are enabled:

- `minifyEnabled = true`
- `shrinkResources = true`
- ProGuard / R8 enabled
- packaging cleanup for extra metadata files

Relevant files:

- [app/build.gradle.kts](app/build.gradle.kts)
- [proguard-rules.pro](app/proguard-rules.pro)

Unsigned release output:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## UI settings

The `Settings` tab currently supports:

- language: Persian / English
- theme: Light / Dark

These preferences are stored locally and restored when the app is opened again.

## Implementation notes

- money is stored and calculated using integer values
- the app does not use `Float` or `Double` for currency math
- Persian font files are loaded from `res/raw`
- all main data is stored in a local Room database

## App info

- `applicationId`: `com.encer.offlinesplitwise`
- `minSdk`: `24`
- `targetSdk`: `36`
- `versionCode`: `1`
- `versionName`: `1.0`
