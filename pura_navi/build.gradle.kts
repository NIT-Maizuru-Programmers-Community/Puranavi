// プロジェクトレベルの build.gradle.kts ファイル
plugins {
    //id("com.android.application") version "8.5.0" apply false
    //id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    //id("com.google.gms.google-services") version "4.3.15" apply false // Firebaseプラグイン
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("com.chaquo.python:gradle:15.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("com.google.gms:google-services:4.3.15") // Firebaseプラグインの依存関係
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}


