# Feasibility: Automated Testing with Firebase Emulator + gdUnit4

## Context

The plugin is growing in scope (6 modules, ~70+ exposed methods). We need to evaluate whether automated testing via Firebase Emulator Suite + gdUnit4 is feasible, and if so, how to set it up in GitHub Actions CI.

---

## Key Finding: The Core Constraint

**gdUnit4 cannot test Kotlin Android plugin code.**

gdUnit4 runs exclusively in the Godot editor / headless mode. Android plugins (.aar + Kotlin) are only loaded inside an exported Android APK — never in the editor. This means gdUnit4 is the wrong tool for the heaviest part of the plugin.

---

## What Can Actually Be Tested

### Layer 1 — Kotlin plugin code (the real logic)
**Tool:** JVM unit tests (JUnit 4/5 + Mockito) run via `./gradlew test`
**Firebase Emulator:** Runs on JVM — use `localhost:port`, no Android emulator required
**Coverage:** Auth, Firestore, Realtime DB, Storage, Remote Config logic
**CI complexity:** Low — standard `ubuntu-latest` runner, install Node.js + Firebase CLI + JDK
**Verdict:** ✅ Feasible and relatively easy

### Layer 2 — GDScript wrapper code (export_scripts_template/)
**Tool:** gdUnit4 headless CLI (`runtest.sh`)
**Problem:** The wrappers call `Engine.get_singleton("GodotFirebaseAndroid")` which returns null outside Android. Tests would need heavy mocking of the plugin singleton interface.
**CI complexity:** Moderate — needs Godot binary in runner, wrapper logic is thin (mostly signal connections + delegation)
**Verdict:** ⚠️ Feasible but low ROI — wrappers are thin delegation layers, not complex logic

### Layer 3 — End-to-end (GDScript → Kotlin → Firebase Emulator)
**Tool:** Android Espresso instrumented tests on Android emulator
**CI complexity:** High — needs `reactivecircle/android-emulator-runner@v2`, slow (~15–20 min per run)
**Verdict:** ⚠️ Feasible but heavyweight for CI

---

## Recommended Approach (Phased)

### Phase 1 — JVM Kotlin Tests + Firebase Emulator (highest value, lowest friction)

Set up `./gradlew test` with Firebase Emulator:

1. Add JUnit 4 test dependencies to `firebase/build.gradle.kts`
2. Create `firebase/src/test/java/.../firebase/` test classes per module
3. Configure Firebase Emulator for JVM: Auth (port 9099), Firestore (8080), Realtime DB (9000), Storage (9199)
4. Tests call Kotlin module methods directly, verify signal emissions via callbacks

**CI workflow additions:**
```yaml
- uses: actions/setup-node@v4
  with: { node-version: '20' }
- run: npm install -g firebase-tools
- run: firebase emulators:start --only auth,firestore,database,storage &
- run: ./gradlew test
```

This gives direct coverage of the actual Firebase SDK integration — no mocking needed because the Emulator is a real Firebase server locally.

### Phase 2 — gdUnit4 for GDScript (optional, if wrappers grow in complexity)

Only worth adding if wrappers gain non-trivial logic (retry policies, caching, error normalization, etc.). Current wrappers are ~20 lines each — not much to test.

### Phase 3 — Android Instrumented Tests (future, for end-to-end confidence)

When the plugin is more mature, Espresso + `android-emulator-runner@v2` gives true end-to-end coverage. The plugin already has `use_emulator()` methods in Auth.gd and presumably Firestore, making this architecturally ready.

---

## Existing Emulator Support in the Plugin

The plugin already anticipates emulator use:
- `Auth.gd` exposes `use_emulator(host, port)` → calls `authentication_use_emulator` on Kotlin
- This means **the architecture is already prepared** for emulator-based testing

---

## CI Feasibility Summary

| Test Strategy | CI Difficulty | Setup Time | Coverage |
|---|---|---|---|
| JVM Kotlin + Firebase Emulator | Low | ~1 day | Auth, Firestore, DB, Storage, Config |
| gdUnit4 headless (GDScript) | Moderate | ~2 days | Wrapper delegation logic only |
| Android Espresso + Emulator | High | ~3–5 days | True end-to-end |

**Short answer:** Yes, it's technically feasible. Phase 1 (JVM tests + Firebase Emulator) is the easiest, highest-value starting point and can run on standard GitHub Actions without an Android emulator.

---

## Critical Files to Modify

- `firebase/build.gradle.kts` — add JUnit test deps
- `firebase/src/test/java/org/godotengine/plugin/firebase/` — new test classes (to be created)
- `.github/workflows/` — update CI to install Firebase CLI and run emulator

## Verification

1. `./gradlew test` passes with emulator running locally
2. Firebase Emulator starts and stops cleanly in CI logs
3. Auth/Firestore/DB/Storage test cases emit expected signals
