object Versions {
    const val android_plugin = "4.0.1"
    const val kotlin = "1.4.0"
    const val appcompat = "1.1.0"
    const val core = "1.3.0"
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
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val spotlessGradle = "com.diffplug.spotless:spotless-plugin-gradle:4.3.1"
}


object Deps {
    const val kotlinLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.core}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"

    const val leakCanary ="com.squareup.leakcanary:leakcanary-android:2.4"

    const val socketIO = "io.socket:socket.io-client:1.0.0"

    const val googleRtc = "org.webrtc:google-webrtc:1.0.30039"

    const val junit = "junit:junit:4.13"
    const val junitExt = "androidx.test.ext:junit:1.1.1"
    const val espresso = "androidx.test.espresso:espresso-core:3.2.0"
}