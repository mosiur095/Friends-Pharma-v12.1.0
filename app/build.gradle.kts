import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.jetbrainsCompose)
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.friendspharma.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.friendspharma.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("friendpharma-upload-key.jks")
            storePassword = localProperties["KEYSTORE_PASSWORD"] as String
            keyAlias = localProperties["KEY_ALIAS"] as String
            keyPassword = localProperties["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //friendspharma
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            // ✅ Fix '-Xannotation-default-target' warnings from Kotlin 2.x
            freeCompilerArgs.addAll(
                listOf("-Xannotation-default-target=param-and-property")
            )
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ✅ FIX 1: Single BOM version for BOTH main and test dependencies
    // Previously two different BOMs (v20240800 vs v20240600) were causing
    // resource ID conflict "No package ID 6a found for resource ID 0x6a0b0013"
    implementation(platform(libs.androidx.compose.bom.v20240800))
    androidTestImplementation(platform(libs.androidx.compose.bom.v20240800))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)

    // ✅ FIX 2: Removed hardcoded "androidx.compose.material:material:1.7.8"
    // The BOM already manages this version — having both caused a second
    // resource ID conflict between material and material3
    implementation(libs.androidx.material.icons.extended)

    // ✅ FIX 3: Removed duplicate junit (was declared 3 times)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    //test
    testImplementation(libs.truth)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.core.testing)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.mockito.android)
    testImplementation(libs.mockito.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.kotlin.mockito.kotlin)

    // Compose dependencies
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.flowlayout)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coroutine Lifecycle Scopes
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Dagger - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    // ✅ New artifact — hiltViewModel() moved here in 1.3.0, fixes deprecation warning
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //image
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.crashlytics.ktx)

    //gson
    implementation(libs.gson)

    //live data
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.accompanist.systemuicontroller)

    //location
    implementation(libs.play.services.location)

    //in app update
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    //benchmark
    implementation(libs.androidx.profileinstaller)

    //webkit
    implementation(libs.androidx.webkit)

    //in app review
    implementation(libs.review)
    implementation(libs.review.ktx)

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    ksp(libs.androidx.room.compiler)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    // optional - RxJava2 support for Room
    implementation(libs.androidx.room.rxjava2)

    // optional - RxJava3 support for Room
    implementation(libs.androidx.room.rxjava3)

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation(libs.androidx.room.guava)

    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)

    implementation(libs.accompanist.pager)

    implementation(libs.facebook.android.sdk)

    implementation(libs.play.services.auth)

    implementation(libs.itext7.core)

    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")
}