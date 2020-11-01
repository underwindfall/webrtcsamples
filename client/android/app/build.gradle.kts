plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
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

val debugImplementation by configurations

dependencies {
    debugImplementation(Deps.leakCanary)
    implementation(project(path = ":webrtc"))
    implementation(Deps.kotlinLibrary)
    implementation(Deps.appcompat)
    implementation(Deps.coreKtx)
    implementation(Deps.constraintLayout)
    implementation(Deps.powerPermission)
    testImplementation(Deps.junit)
    androidTestImplementation(Deps.junitExt)
    androidTestImplementation(Deps.espresso)
}
