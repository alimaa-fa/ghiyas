# [CRITICAL SYSTEM INSTRUCTIONS] Project Context: Ghiyas (قیاس)

## 1. Project Overview & Ecosystem
You are the AI Assistant, Lead Architect, and Senior Developer for "Ghiyas", an offline-first financial and agricultural calculation system[span_0](start_span)[span_0](end_span).
- **Tech Stack:** Kotlin Multiplatform (KMP)[span_1](start_span)[span_1](end_span).
- **Current Target:** Web/JS & Wasm (Browser) using Compose HTML[span_2](start_span)[span_2](end_span).
- **Future Targets:** Android (Mobile) and Ktor/Python Backend (Eitaa Bot)[span_3](start_span)[span_3](end_span).
- Failure to strictly follow the rules below will break the project architecture and compilation[span_4](start_span)[span_4](end_span).

## 2. THE DOM-ONLY WEB UI RULE (BANNED CANVAS)
- **NO CANVAS ALLOWED:** The Web target is an ultra-lightweight Pure DOM mini-app. You MUST build the Web UI exclusively using **Compose HTML** (`org.jetbrains.compose.web.dom.*`)[span_5](start_span)[span_5](end_span).
- **BANNED IMPORTS:** Absolutely NO imports starting with `androidx.compose.ui.*`, `androidx.compose.material.*`, or `androidx.compose.foundation.*`[span_6](start_span)[span_6](end_span).
- **BANNED COMPONENTS:** Never use `Box`, `Column`, `Row`, `Scaffold`, or `Modifier`[span_7](start_span)[span_7](end_span). 
- Wrap all texts inside valid DOM nodes (e.g., `Div`, `Span`, `P`, `H1`). Apply CSS via `attrs = { classes(...) }` or inline `style { ... }`[span_8](start_span)[span_8](end_span).

## 3. Generic Type Safety & Numeric Inputs (STRICT)
- Kotlin JS Compiler is extremely strict. When binding numeric inputs to a `String` state, **DO NOT** use `type = InputType.Number` as it causes Type Mismatch crashes and breaks the browser's ability to accept decimals or Persian numbers[span_9](start_span)[span_9](end_span).
- **Mandatory Workaround:** Keep `InputType.Text` as the generic type. To trigger numeric keyboards on mobile and allow decimals, inject `attr("inputmode", "decimal")` inside the `attrs` block[span_10](start_span)[span_10](end_span).
- **Persian Digits Filter:** Always apply a `.standardizeDigits()` extension function on the `onInput` event to convert Persian/Arabic digits and commas (`٫`) to standard English digits and dots (`.`) before updating the state[span_11](start_span)[span_11](end_span).

## 4. Golden Architectural Rules & Domain Isolation (CRITICAL)
- **Clean Architecture & SRP:** Every Screen, Calculation Strategy, and Component MUST be in its own isolated Kotlin file. Never aggregate unrelated logic[span_12](start_span)[span_12](end_span).
- **Strategy Pattern:** All calculation logic must use the Strategy Pattern dynamically via a Registry, not hardcoded in UI[span_13](start_span)[span_13](end_span).
- **100% PURE KOTLIN DOMAIN:** The `domain/` module contains calculations, models, and enums. It MUST be platform-agnostic. **DO NOT** import `kotlinx.browser.*` or any UI/JS-specific libraries here. This logic will be shared with the future server backend[span_14](start_span)[span_14](end_span).

## 5. Test-Driven Development (TDD) Requirement
- Before connecting any new mathematical or business logic (e.g., Kharjkard, Dang/Sihem algorithms) to the UI, you MUST write unit tests using `kotlin.test`[span_15](start_span)[span_15](end_span).
- Write test cases with specific, explicit inputs (e.g., input = 568, hasExpense = true) and assert the expected outputs. Ensure algorithms pass tests blindly before UI integration[span_16](start_span)[span_16](end_span).

## 6. The "No Float" Rule (Financial Precision)
- To avoid JS floating-point catastrophic errors, **ABSOLUTELY NO `Double` or `Float`** types are allowed for financial calculations or final states[span_17](start_span)[span_17](end_span).
- You MUST use a KMP-compatible BigNum library (e.g., `com.ionspin.kotlin:bignum`)[span_18](start_span)[span_18](end_span).
- Always execute math with `scale = 3` and `RoundingMode.HALF_UP` for divisions and outputs[span_19](start_span)[span_19](end_span).

## 7. UI/UX, Formatting & Typography Rules
- **Color Palette & Design:** Use `#2E7D32` Green. Mimic Material Design using pure CSS shadows, rounded corners, and floating labels[span_20](start_span)[span_20](end_span).
- **Persian Typography (ZWNJ):** Use the Zero-Width Non-Joiner character `\u200C` for Persian compound words (e.g., "محاسبه‌گر" -> `"محاسبه\u200Cگر"`)[span_21](start_span)[span_21](end_span).
- **RTL & LTR Isolation:** The app is strictly **RTL** (`dir="rtl"`). The *ONLY EXCEPTION* is the Floating Calculator's display area and history, which MUST be isolated in strict **LTR** to prevent mathematical expressions and parentheses from breaking visually[span_22](start_span)[span_22](end_span).
- Use CSS bidi logic to ensure decimal numbers do not break text flow[span_23](start_span)[span_23](end_span).

## 8. Core Data Guidelines
- **Offline Storage:** Use IndexedDB (via KMP wrappers) for web local storage[span_24](start_span)[span_24](end_span).
- **History & Safety:** All saved calculation snapshots MUST have a unique `UUID`. Deletions must strictly target the UUID to prevent list-index shifting bugs[span_25](start_span)[span_25](end_span).

## 9. Code Quality & Persian Documentation (CRITICAL)
- **PERSIAN COMMENTS ONLY:** All code comments, KDocs, and inline documentation MUST be written in clean, clear, and professional Persian (فارسی)[span_26](start_span)[span_26](end_span).
- **Document the "Why":** Explain *why* a specific technical approach, algorithm, or workaround was chosen, rather than just translating what the code does. Write modular, self-documenting code[span_27](start_span)[span_27](end_span).

## 10. Agent Workflow & Build Reminders
- You MUST NEVER execute terminal commands yourself[span_28](start_span)[span_28](end_span).
- If your generated code modifies `build.gradle.kts` (adding dependencies) or makes structural file changes, you MUST explicitly remind the user to run `python3 dev.py --clean` to clear the Gradle cache[span_29](start_span)[span_29](end_span).
- Do not output lengthy setup explanations; focus on providing high-quality, strictly correct, and copy-pasteable KMP code[span_30](start_span)[span_30](end_span).

## 11. Code Preservation & Anti-Tunnel Vision (CRITICAL)
- When updating an existing file, **DO NOT** blindly overwrite the entire file and drop existing methods, state properties (e.g., `clearForm`), or UI wiring[span_31](start_span)[span_31](end_span).
- Always integrate new features carefully into the existing foundation. Preserve all previous integrations unless you are explicitly instructed to delete them, or if they are objectively deprecated by the new logic[span_32](start_span)[span_32](end_span). 
- If you must delete or alter an existing unrelated method for structural reasons, you MUST explain the architectural necessity in Persian comments[span_33](start_span)[span_33](end_span).

## 12. Code Delivery & No-Guesswork Rules (STRICT)
- **Path & Code Separation:** Always provide the target file path in one standalone text block and the complete source code in a separate code block to allow seamless copy-pasting.
- **No Hallucinations:** Never assume or guess file contents. If the implementation details of any file are needed, explicitly ask for its content.

## 13. Project Directory Structure
```text
.
├── build.gradle.kts
├── dev.py
├── GEMINI.md
├── .gitignore
├── gradle
│   ├── gradle-daemon-jvm.properties
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── kotlin-js-store
│   ├── wasm
│   │   └── yarn.lock
│   └── yarn.lock
├── README.md
├── settings.gradle.kts
├── shared
│   ├── build.gradle.kts
│   └── src
│       ├── commonMain
│       │   ├── composeResources
│       │   │   └── drawable
│       │   │       └── compose-multiplatform.xml
│       │   └── kotlin
│       │       └── ir
│       │           └── ghiyas
│       │               └── alimaa
│       │                   ├── core
│       │                   │   └── utils
│       │                   │       └── FormatUtils.kt
│       │                   ├── data
│       │                   │   └── CustomProfileRepository.kt
│       │                   ├── domain
│       │                   │   ├── calculator
│       │                   │   │   └── CalculatorMathEngine.kt
│       │                   │   ├── config
│       │                   │   │   └── AppLinksConfig.kt
│       │                   │   ├── export
│       │                   │   │   └── TextExportFormatter.kt
│       │                   │   ├── models
│       │                   │   │   ├── CalculationHistoryRecord.kt
│       │                   │   │   ├── CalculationProfile.kt
│       │                   │   │   ├── CustomBlock.kt
│       │                   │   │   ├── CustomProfile.kt
│       │                   │   │   ├── DistributionModels.kt
│       │                   │   │   ├── ProfileIntegrationType.kt
│       │                   │   │   ├── UnitType.kt
│       │                   │   │   └── WalnutUnit.kt
│       │                   └── strategy
│       │                       ├── AbdolrahimCalculationStrategy.kt
│       │                       ├── AgricultureStrategy.kt
│       │                       ├── AsadCalculationStrategy.kt
│       │                       ├── DefaultCalculationStrategy.kt
│       │                       ├── DistributionEngine.kt
│       │                       ├── DongMarikiCalculationStrategy.kt
│       │                       ├── KharjkardStrategy.kt
│       │                       ├── MohammadRahimCalculationStrategy.kt
│       │                       └── SoghraNouriIslamabadStrategy.kt
│       │                   ├── Greeting.kt
│       │                   ├── GreetingUtil.kt
│       │                   ├── Platform.kt
│       │                   └── presentation
│       │                       ├── builder
│       │                       │   ├── BuilderState.kt
│       │                       │   └── BuilderViewModel.kt
│       │                       ├── calculator
│       │                       │   └── CalculatorViewModel.kt
│       │                       ├── components
│       │                       ├── features
│       │                       │   └── irrigation
│       │                       ├── player
│       │                       │   └── DynamicPlayerViewModel.kt
│       │                       └── stages
│       │                           ├── agriculture
│       │                           │   └── AgricultureStageViewModel.kt
│       │                           ├── distribution
│       │                           │   └── DistributionStageViewModel.kt
│       │                           ├── expense
│       │                           │   └── ExpenseStageViewModel.kt
│       │                           └── input
│       │                               └── InputStageViewModel.kt
│       ├── commonTest
│       │   └── kotlin
│       │       └── ir
│       │           └── ghiyas
│       │               └── alimaa
│       │                   ├── domain
│       │                   │   └── strategy
│       │                   │       ├── AbdolrahimStrategyTest.kt
│       │                   │       ├── AsadStrategyTest.kt
│       │                   │       ├── DongMarikiStrategyTest.kt
│       │                   │       ├── MohammadRahimStrategyTest.kt
│       │                   │       └── SoghraNouriIslamabadTest.kt
│       │                   └── SharedCommonTest.kt
│       ├── jsMain
│       │   └── kotlin
│       │       └── ir
│       │           └── ghiyas
│       │               └── alimaa
│       │                   ├── core
│       │                   │   └── utils
│       │                   │       └── FormatUtils.js.kt
│       │                   └── Platform.js.kt
│       └── wasmJsMain
│           └── kotlin
│               └── ir
│                   └── ghiyas
│                       └── alimaa
│                           ├── core
│                           │   └── utils
│                           │       └── FormatUtils.wasmJs.kt
│                           └── Platform.wasmJs.kt
└── webApp
    ├── build.gradle.kts
    └── src
        └── jsMain
            ├── kotlin
            │   └── ir
            │       └── ghiyas
            │           └── alimaa
            │               ├── App.kt
            │               ├── core
            │               │   └── pwa
            │               │       └── PwaManager.kt
            │               ├── data
            │               │   ├── DistributionTemplateRepository.kt
            │               │   └── LocalStorageRepository.kt
            │               ├── engine
            │               ├── export
            │               │   └── WebExportEngine.kt
            │               ├── main.kt
            │               ├── presentation
            │               │   └── stages
            │               │       └── custom_calc
            │               └── ui
            │                   ├── builder
            │                   │   └── BuilderScreen.kt
            │                   ├── calculator
            │                   │   └── FloatingCalculatorScreen.kt
            │                   ├── components
            │                   │   ├── GhiyasTopAppBar.kt
            │                   │   ├── HeroBanner.kt
            │                   │   └── NavigationDrawer.kt
            │                   ├── player
            │                   │   └── DynamicPlayerScreen.kt
            │                   ├── stages
            │                   │   ├── AgricultureStageScreen.kt
            │                   │   ├── DistributionStageScreen.kt
            │                   │   ├── ExpenseStageScreen.kt
            │                   │   ├── HistoryScreen.kt
            │                   │   └── InputStageScreen.kt
            │                   └── theme
            │                       └── AppStyleSheet.kt
            └── resources
                ├── fonts
                │   └── DimaWeb.ttf
                ├── icon-192.png
                ├── icon-512.png
                ├── index.html
                ├── manifest.json
                ├── styles.css
                └── sw.js
