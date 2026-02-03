buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Hum 8.1.0 use karenge jo sabse stable hai
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
