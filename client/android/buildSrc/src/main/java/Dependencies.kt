object Versions {
    const val android_plugin = "3.6.3"
    const val gms_plugin = "4.3.3"
    const val firebase_plugin = "1.1.0"
    const val kotlin = "1.3.72"
    const val appcompat = "1.1.0"
    const val core = "1.2.0"
    const val jetpack_lifecycle = "2.2.0"
    const val firebase_firestore = "21.4.3"
    const val firebase_functions = "19.0.2"
}

object BuildVersions {
    const val compileSdkVersion = 29
    const val minSdkVersion = 21
    const val targetSdkVersion = 29
    const val versionCode = 1
    const val versionName = "0.0.1"
}

object BuildPlugins {
    const val androidGradle = "com.android.tools.build:gradle:${Versions.android_plugin}"
    const val googleServicesGradle = "com.google.gms:google-services:${Versions.gms_plugin}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val fbAppDistributeGradle = "com.google.firebase:firebase-appdistribution-gradle:${Versions.firebase_plugin}"
}


object Deps {
    const val kotlinLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.core}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"

    const val lifecycleJava8 = "androidx.lifecycle:lifecycle-common-java8:${Versions.jetpack_lifecycle}"
    const val lifecycleKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.jetpack_lifecycle}"

    const val socketIO = "io.socket:socket.io-client:1.0.0"
    const val firestore = "com.google.firebase:firebase-firestore:${Versions.firebase_firestore}"
    const val firestoreKtx = "com.google.firebase:firebase-firestore-ktx:${Versions.firebase_firestore}"
    const val functions = "com.google.firebase:firebase-functions:${Versions.firebase_functions}"
    const val functionsKtx = "com.google.firebase:firebase-functions-ktx:${Versions.firebase_functions}"

    const val googleRtc = "org.webrtc:google-webrtc:1.0.30039"
    const val netatmoRtc= "com.netatmo:webrtc:0.0.2@aar"

    const val junit = "junit:junit:4.13"
    const val junitExt = "androidx.test.ext:junit:1.1.1"
    const val espresso = "androidx.test.espresso:espresso-core:3.2.0"
}