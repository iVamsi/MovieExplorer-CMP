plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("movieapp.detekt")
}

android {
    namespace = "com.movieapp"
    compileSdk = catalogVersion("compileSdk").toInt()
    defaultConfig {
        applicationId = "com.movieapp.cmp"
        minSdk = catalogVersion("minSdk").toInt()
        targetSdk = catalogVersion("targetSdk").toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures { compose = true }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
