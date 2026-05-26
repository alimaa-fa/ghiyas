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
            // این خط برای رفع خطای Composition حیاتی است:
            implementation(compose.runtime)
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
    }
}

// راه حل ۱۰۰٪ استاندارد و سازگار با سیستم کش گریدل: استفاده از تسک اختصاصی Copy
val copyFontsTask = tasks.register<Copy>("copyFontsTask") {
    from("src/jsMain/resources/fonts")
    into("build/kotlin-webpack/js/developmentExecutable/fonts")
}

// به گریدل می‌گوییم هر وقت تسک وب‌پک به طور کامل تمام شد، تسک کپی ما را اجرا کند (بدون دخالت در لاجیک وب‌پک)
tasks.matching { it.name.contains("BrowserDevelopmentWebpack") }.configureEach {
    finalizedBy(copyFontsTask)
}
