# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GodotFirebaseAndroid is an Android plugin for Godot Engine (4.2+) that bridges GDScript and the native Android Firebase SDK. It follows a dual-layer architecture: Kotlin classes handle Firebase SDK interaction and async operations on the Android side, while thin GDScript wrappers in `firebase/export_scripts_template/` provide the developer-facing API via signals.

**Communication flow:** GDScript → Module .gd wrapper → Kotlin plugin singleton → Firebase SDK → Firebase Backend. All long-running operations use Godot signals (not blocking calls).

## Build Commands

```bash
./gradlew assemble    # Build debug+release AARs, copy to demo/addons/
./gradlew build       # Build AARs without copying
./gradlew clean       # Clean build outputs and demo addons
```

The build produces `firebase-debug.aar` and `firebase-release.aar`, copies them to `demo/addons/GodotFirebaseAndroid/bin/`, and copies `export_scripts_template/` into the demo addon directory.

There are no tests or linters configured.

## Architecture

### Kotlin layer (`firebase/src/main/java/org/godotengine/plugin/firebase/`)

- **FirebasePlugin.kt** — Main Godot plugin class. Registers all signals and delegates calls to module classes. All module classes are instantiated here and receive the plugin reference.
- **Authentication.kt** — Anonymous, email/password, Google Sign-In, email verification, password reset, account linking.
- **Firestore.kt** — CRUD operations, collection queries, real-time listeners.
- **RealtimeDatabase.kt** — Path-based CRUD, real-time listeners.
- **CloudStorage.kt** — Upload/download, metadata, file listing.
- **Analytics.kt** — Event logging, user properties/ID.
- **RemoteConfig.kt** — Fetch, activate, typed getters.

Each Kotlin module emits signals back to Godot via `emitSignal()` for async results.

### GDScript layer (`firebase/export_scripts_template/`)

- **Firebase.gd** — Autoloaded singleton that initializes module wrappers.
- **modules/*.gd** — One wrapper per Firebase module. Each connects to the Kotlin plugin singleton's signals and exposes snake_case methods.
- **export_plugin.gd** — Godot export plugin that handles adding dependencies and `google-services.json` during Android export.
- **plugin.cfg** — Godot plugin descriptor.

### Demo project (`demo/`)

Contains a Godot project with one scene per module for manual testing. The demo's `addons/` directory is auto-populated by the build.

## Key Details

- **Godot version:** 4.6+ (uses Godot Android plugin v2 API)
- **Android:** minSdk 24, targetSdk 34
- **Kotlin DSL** for all Gradle files
- **No `google-services.json` in repo** — must be supplied per-project in `demo/android/build/`
- **Fork:** origin is SomniGameStudios, upstream is syntaxerror247/GodotFirebaseAndroid
- **PRs:** Always open pull requests against the fork repo (`SomniGameStudios/godot-firebase-android`), never against upstream

## Adding a New Firebase Module

1. Create Kotlin class in `firebase/src/main/java/.../firebase/` implementing the Firebase SDK calls with `emitSignal()` callbacks
2. Register signals and expose methods in `FirebasePlugin.kt`
3. Create GDScript wrapper in `firebase/export_scripts_template/modules/`
4. Initialize the module in `Firebase.gd`
5. Add Firebase dependency in `firebase/build.gradle.kts`
6. Add demo scene in `demo/scenes/`
