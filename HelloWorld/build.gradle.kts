// トップレベルのbuild.gradleファイル。すべてのサブプロジェクト/モジュールに共通する設定を追加できます。
plugins {
<<<<<<< Updated upstream
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.chaquo.python") version "15.0.1" apply false
=======
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.chaquo.python") version "15.0.1" apply false  // Chaquopyプラグイン
    alias(libs.plugins.google.gms.google.services) apply false  // Firebaseプラグイン
>>>>>>> Stashed changes
}

buildscript {
    repositories {
        google()
<<<<<<< Updated upstream
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // Remove duplicate or incorrect versions
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath("com.chaquo.python:gradle:15.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter()
    }
}
=======
        mavenCentral()  // 推奨されるMaven Centralリポジトリを使用
    }
    dependencies {
        // Android Gradle Pluginの最新安定版
        classpath("com.android.tools.build:gradle:8.7.0")
        // Chaquopy Gradle Pluginを追加
        classpath("com.chaquo.python:gradle:10.0.1")
    }
}
>>>>>>> Stashed changes
