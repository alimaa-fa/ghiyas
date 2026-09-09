plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "ir.ghiyas.app"
    // تنظیم کامپایلر برای اندروید 17
    compileSdk = 37

    defaultConfig {
        applicationId = "ir.ghiyas.app"
        minSdk = 21
        // تنظیم تارگت نهایی برای اندروید 17
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
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
        // نسخه زبان جاوا 17 (نیاز ضروری کاتلین و بیلد سیستم)
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.10.0")
}
