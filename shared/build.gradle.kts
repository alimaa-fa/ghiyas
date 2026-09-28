plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    js {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            // رندر خالص دام بر پایه Compose HTML بدون کانواس
            implementation(libs.compose.html.core)
            implementation(libs.compose.html.svg)
            
            implementation(libs.kotlinx.coroutines.core)
            // استفاده از api جهت دسترسی شفاف ماژول‌های وب و کلاینت به سوپرتایپ‌های سریال‌سازی
            api(libs.kotlinx.serialization.json)
            // استفاده از api جهت انتقال امن تایپ‌های مالی بدون ممیز شناور به لایه‌های مصرف‌کننده
            api(libs.bignum)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}
