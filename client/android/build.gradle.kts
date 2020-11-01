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

plugins {
    id("com.github.ben-manes.versions") version "0.33.0"
}

apply {
    from(rootProject.file("spotless/spotless.gradle"))
}

allprojects {
    repositories {
        google()
        jcenter()
    }

    configurations.all {
        resolutionStrategy.componentSelection {
            all {
                val rejected = listOf("alpha", "beta", "rc", "cr", "m")
                    .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                    .any { it.matches(candidate.version) }
                if (rejected) {
                    reject("Not stable")
                }
            }
        }
    }
}
