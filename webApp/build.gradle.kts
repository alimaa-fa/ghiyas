plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// تسک استخراج نسخه از ماژول اندروید و تزریق آن به کدهای وب
val generateAppConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/appConfig/jsMain/kotlin/ghiyas/alimaa/fa").get().asFile
    val outputFile = File(outputDir, "AppConfig.kt")
    val androidGradleFile = file("../androidApp/build.gradle.kts")
    
    inputs.file(androidGradleFile)
    outputs.file(outputFile)
    
    doLast {
        var androidVersion = "نامشخص"
        if (androidGradleFile.exists()) {
            val content = androidGradleFile.readText()
            // استخراج versionName با استفاده از Regex
            val regex = """versionName\s*=\s*"([^"]+)"""".toRegex()
            val match = regex.find(content)
            if (match != null) {
                androidVersion = match.groupValues[1]
            }
        }
        
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            package ghiyas.alimaa.fa
            
            // این فایل به صورت خودکار توسط Gradle ساخته می‌شود
            object AppConfig {
                const val ANDROID_VERSION = "$androidVersion"
            }
        """.trimIndent())
    }
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
            implementation(libs.kotlinx.coroutines.core) 
        }

        jsMain.dependencies {
            implementation(compose.html.core)
        }
        
        // اضافه کردن پوشه کدهای جنریت شده به سورس‌های جاوااسکریپت
        jsMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/appConfig/jsMain/kotlin"))
        }
    }
}

// اطمینان از اجرای تسک جنریت قبل از کامپایل JS
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().configureEach {
    dependsOn(generateAppConfig)
}

val copyFontsTask = tasks.register<Copy>("copyFontsTask") {
    from("src/jsMain/resources/fonts")
    into("build/kotlin-webpack/js/developmentExecutable/fonts")
}

tasks.matching { it.name.contains("BrowserDevelopmentWebpack") }.configureEach {
    finalizedBy(copyFontsTask)
}
