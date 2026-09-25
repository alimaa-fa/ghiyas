plugins {
    alias(libs.plugins.androidApplication)
    kotlin("android")
}

android {
    namespace = "ghiyas.alimaa.fa"
    // تغییر به نسخه پایدار ۳۴ جهت رفع خطای سرور ابری گیت‌هاب
    compileSdk = 34

    defaultConfig {
        applicationId = "ghiyas.alimaa.fa"
        minSdk = 21
        // تغییر به نسخه پایدار ۳۴
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.22"
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
