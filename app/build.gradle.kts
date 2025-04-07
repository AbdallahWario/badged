plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.defalsified.android.badged"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.defalsified.android.badged"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")) // Native archs
        }

        externalNativeBuild {
            cmake {
                cFlags.addAll(listOf("-DANDROID_STL=c++_shared", "-DANDROID_PLATFORM=android-24")) // C flags
                cppFlags.addAll(listOf("-std=c++17")) // C++ flags
                arguments.addAll(listOf("-DANDROID_STL=c++_shared", "-DANDROID_PLATFORM=android-24")) // CMake args
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }

    ndkVersion = "25.1.8937393" // NDK version

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("com.google.zxing:core:3.5.1")
}