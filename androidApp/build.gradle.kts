plugins {
    alias(libs.plugins.androidApplication)
    // در AGP 9.0 به بعد، پشتیبانی کاتلین به صورت بومی ادغام شده و نیازی به پلاگین مجزای kotlin("android") نیست
}

android {
    namespace = "ghiyas.alimaa.fa"
    // تنظیم کامپایل بر پایه اندروید ۱۶ (API 36) با پشتیبانی زیرساختی تا API 37
    compileSdk = 36

    defaultConfig {
        applicationId = "ghiyas.alimaa.fa"
        minSdk = 21
        // هدف‌گذاری اندروید ۱۶ برای اجرای پایدار روی دستگاه‌های جدید
        targetSdk = 36
        versionCode = 40
        versionName = "1.2.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // استفاده از جاوا ۱۷ برای سازگاری کامل با کامپایلر مدرن اندروید
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // کتابخانه‌های هسته اندروید هماهنگ با API 36
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
