plugins {
    id("com.android.application")
    kotlin("android.extensions")
    id("kotlin-android")
}

android {
    compileSdkVersion(BuildVersions.compileSdkVersion)

    defaultConfig {
        applicationId = "com.qifan.webrtcsamples"
        minSdkVersion(BuildVersions.minSdkVersion)
        targetSdkVersion(BuildVersions.targetSdkVersion)
        versionCode = BuildVersions.versionCode
        versionName = BuildVersions.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        val options = this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
        options.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(Deps.kotlinLibrary)
    implementation(Deps.appcompat)
    implementation(Deps.coreKtx)
    implementation(Deps.constraintLayout)
    implementation(Deps.googleRtc)
    implementation(Deps.socketIO) {
        exclude(group = "org.json", module = "json")
    }
    testImplementation(Deps.junit)
    androidTestImplementation(Deps.junitExt)
    androidTestImplementation(Deps.espresso)
}
