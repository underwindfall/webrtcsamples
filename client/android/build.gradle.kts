buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(BuildPlugins.androidGradle)
        classpath(BuildPlugins.kotlinGradle)
        classpath(BuildPlugins.spotlessGradle)
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
