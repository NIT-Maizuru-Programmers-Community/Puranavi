
plugins {
<<<<<<< Updated upstream
    id("com.android.application") version "8.1.1" apply false
    id ("kotlin-android")
    id ("com.chaquo.python") version "15.0.1"
=======
    id("com.android.application")
    id("kotlin-android")
    id("com.chaquo.python") version "15.0.1"  // Chaquopyプラグイン
    id("com.google.gms.google-services")  // Firebaseプラグイン
>>>>>>> Stashed changes
}

android {
    namespace = "com.example.helloworld"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.helloworld"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // Python実行ファイルのパス設定（Windows環境の場合）
        buildConfigField("String", "PYTHON_EXECUTABLE", "\"C:/Users/syake/AppData/Local/Programs/Python/Python312/python.exe\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

chaquopy {
    defaultConfig {
        // Pythonビルドの設定
        buildPython("\"C:/Users/waken/AppData/Local/Programs/Python/Python312/python.exe\"")

        pip {
            // 必要なPythonライブラリをインストール
            install("numpy")
            install("pandas")
            install("Pillow")
            install("opencv-python")
        }
    }
}

dependencies {
<<<<<<< Updated upstream
    implementation("androidx.activity:activity-compose:1.7.2") // これは最新のバージョンです
    implementation("androidx.compose.ui:ui:1.5.1") // 最新バージョン
    implementation("androidx.compose.ui:ui-graphics:1.5.1") // 最新バージョン
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1") // 最新バージョン
    implementation("androidx.compose.material3:material3:1.3.0") // 最新バージョン
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1") // 最新バージョンを確認
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // 最新バージョンを使用
    implementation ("androidx.appcompat:appcompat:1.5.1")
    implementation ("androidx.core:core-ktx:1.9.0")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.0")
    implementation(project(":OpenCV4And"))
    implementation("com.android.support:appcompat-v7:28.0.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1") // 最新バージョン
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1") // 最新バージョン
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1") // 最新バージョン
=======
    // Android関連
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.ui:ui-graphics:1.7.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
>>>>>>> Stashed changes

    // HoloColorPickerライブラリ
    implementation("com.larswerkman:HoloColorPicker:1.5")

    // Firebase関連
    implementation("com.google.firebase:firebase-database:21.0.0")

    // テスト関連
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.3")
}
