pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Chaquopyリポジトリ
        maven(url = "https://chaquo.com/maven")
        maven { url = uri("https://plugins.gradle.org/m2/") }
        // Maven Centralリポジトリ
        mavenCentral()
        google()
        // Gradleプラグインポータル
        gradlePluginPortal()
        // JitPackリポジトリ
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPackリポジトリ
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "HelloWorld"

// サブプロジェクトのインクルード
include(":app")
