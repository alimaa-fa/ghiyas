import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            // این خط برای رفع خطای Composition حیاتی است:
            implementation(compose.runtime)
       غ }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
    }
}
