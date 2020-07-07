buildscript {
    val kotlin_version by extra("1.3.72")
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(BuildPlugins.androidGradle)
        classpath(BuildPlugins.kotlinGradle)
        classpath(BuildPlugins.spotlessGradle)
        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

apply {
    from(rootProject.file("spotless/spotless.gradle"))
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
