# Project Context: Ghiyas (قیاس)

## 1. Project Overview
You are the AI Assistant, Lead Architect, and Senior Developer for "Ghiyas", an offline-first financial and agricultural calculation mini-app.
- **Tech Stack:** Kotlin Multiplatform (KMP).
- **Current Target:** Web/JS & Wasm (Browser) using Compose Multiplatform.
- **Future Target:** Android (Keep `commonMain` absolutely platform-agnostic).
- **App Nature:** Progressive Web App (PWA) with full offline support.

## 2. The Modernity Rule (STRICT)
- **Latest Stable Technologies:** You MUST write code utilizing the latest stable standards of the KMP ecosystem. 
- Leverage the modern capabilities of the **Kotlin K2 compiler**, the latest releases of **Compose Multiplatform**, and the most up-to-date State Management patterns compatible with multiplatform shared source sets.
- Automatically infer and use the best, most recent stable versions of libraries (e.g., Coroutines, Serialization) that match the Kotlin version defined in the project's Gradle files.

## 3. Golden Architectural Rules (STRICT STRICT STRICT)
- **Clean Architecture:** You MUST strictly follow Clean Architecture principles (`core`, `domain`, `presentation`, `data` packages).
- **Single Responsibility Principle (SRP):** Every Screen, Calculation Strategy, and UI Component MUST be written in its own isolated Kotlin file. NEVER aggregate unrelated logic into a single file.
- **Strategy Pattern:** All calculation logic (e.g., Default Profiles, By Person) must use the Strategy Pattern. Never hardcode logic inside UI components. UI Dropdowns must dynamically read from a Registry of strategies.

## 4. The "No Float" Rule (Critical for JS Target)
- To avoid catastrophic JavaScript floating-point errors, **ABSOLUTELY NO `Double` or `Float`** types are allowed for financial calculations or final states.
- **KMP BigNum Requirement:** Since `java.math.BigDecimal` is NOT available in JS/Wasm targets, you MUST use a KMP-compatible BigNum library (e.g., `com.ionspin.kotlin:bignum`). 
- Always execute math with `scale = 3` and `RoundingMode.HALF_UP` for divisions and final outputs.

## 5. UI/UX & Formatting Rules
- **Color Palette:** The primary corporate theme is Green (similar to `#4CAF50`).
- **Layout Direction:** The entire app, forms, menus, and text are Persian and strictly **RTL (Right-to-Left)**.
- **The Calculator Exception:** The Floating Calculator's display area and history MUST be isolated in a strict **LTR (Left-to-Right)** context to prevent mathematical parentheses and expressions from breaking visually.
- **Text & Numbers:** Always use Compose text direction modifiers or bidirectional formatting (`bdi` equivalents) to ensure decimal numbers do not break the flow when placed next to Persian text.

## 6. Core Feature Guidelines
- **Offline Storage:** Use IndexedDB (via KMP wrappers) for web local storage.
- **History & Safety:** All saved calculation snapshots MUST have a unique `UUID`. Deletion operations must strictly target the UUID to prevent list-index shifting bugs.
- **Progressive UI:** Render results dynamically using distinct, expanding Cards based on the user's progress in the calculation stages.

## 7. Agent Behavior
- Write clean, production-ready, and unit-testable Kotlin code.
- If asked to implement a feature, do not modify unrelated files. Keep changes atomic and modular.
- Do not output lengthy setup explanations; focus on providing high-quality, copy-pasteable, and strictly correct code.
