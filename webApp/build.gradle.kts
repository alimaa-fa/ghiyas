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

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(compose.runtime)
            // تزریق وابستگی کوروتین برای شناسایی StateFlow و CoroutineScope
            implementation(libs.kotlinx.coroutines.core) 
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }
    }
}

val copyFontsTask = tasks.register<Copy>("copyFontsTask") {
    from("src/jsMain/resources/fonts")
    into("build/kotlin-webpack/js/developmentExecutable/fonts")
}

tasks.matching { it.name.contains("BrowserDevelopmentWebpack") }.configureEach {
    finalizedBy(copyFontsTask)
}
