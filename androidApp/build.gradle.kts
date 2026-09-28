plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "ghiyas.alimaa.fa"
    compileSdk = 36

    defaultConfig {
        applicationId = "ghiyas.alimaa.fa"
        minSdk = 21
        targetSdk = 36
        versionCode = 44
        versionName = "1.2.5"
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

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
    // اعمال کتابخانه اسپلش اسکرین برای لود نیتیو
    implementation(libs.androidx.core.splashscreen)
}
