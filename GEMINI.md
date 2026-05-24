# Project Context: Ghiyas (قیاس)

## 1. Project Overview
You are the AI Assistant and Lead Developer for "Ghiyas", a financial and agricultural calculation mini-app.
- **Tech Stack:** Kotlin Multiplatform (KMP).
- **Target:** Web/JS (Browser) using Compose Multiplatform. (Currently NO Android/iOS).
- **UI Framework:** Compose Multiplatform.

## 2. Golden Architectural Rules (STRICT STRICT STRICT)
- **Clean Architecture:** You MUST strictly follow Clean Architecture principles (core, domain, presentation, data).
- **Single Responsibility Principle (SRP):** Every Screen, Calculation Strategy, and UI Component MUST be written in its own isolated Kotlin file. NEVER aggregate unrelated logic into a single file.
- **Strategy Pattern:** All calculation logic (e.g., Default Profiles, By Person) must use the Strategy Pattern. Never hardcode logic inside UI components. UI Dropdowns must dynamically read from a Registry of strategies.

## 3. The "No Float" Rule (Critical for JS Target)
- To avoid catastrophic JavaScript floating-point errors, **ABSOLUTELY NO `Double` or `Float`** types are allowed for financial calculations or final states.
- ALL math and business logic MUST be executed using **`BigDecimal`**.
- Always use `scale = 3` and `RoundingMode.HALF_UP` for divisions and outputs.

## 4. UI/UX & Formatting Rules
- **Color Palette:** The primary corporate theme is Green.
- **Layout Direction:** The entire app, forms, menus, and text are Persian and strictly **RTL (Right-to-Left)**.
- **The Calculator Exception:** The Floating Calculator's display area and history MUST be isolated in a strict **LTR (Left-to-Right)** context to prevent mathematical parentheses and expressions from breaking visually.
- **Text & Numbers:** Always use Compose text direction modifiers or bidirectional formatting to ensure decimal numbers do not break the flow when placed next to Persian text.

## 5. Agent Behavior
- Write clean, production-ready, and unit-testable Kotlin code.
- If asked to implement a feature, do not modify unrelated files.
- Keep changes atomic and modular.
