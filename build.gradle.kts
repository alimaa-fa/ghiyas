import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    // جلوگیری از بارگذاری چندباره پلاگین‌ها در کلاس‌لودر زیرپروژه‌ها
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// تنظیمات اختصاصی مدیریت قفل وابستگی‌های Yarn در ماژول وب KMP
rootProject.plugins.withType<YarnPlugin> {
    rootProject.the<YarnRootExtension>().apply {
        // جایگزینی خودکار فایل قفل در صورت بروزرسانی وابستگی‌ها بدون شکست بیلد
        yarnLockAutoReplace = true
        // نمایش اخطار به جای توقف فرآیند کامپایل در گیت‌هاب اکشن و محیط توسعه
        yarnLockMismatchReport = YarnLockMismatchReport.WARNING
    }
}
