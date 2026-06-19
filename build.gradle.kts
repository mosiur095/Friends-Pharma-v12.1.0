// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    id("com.android.library") version "8.12.3" apply false
    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
    id("com.google.dagger.hilt.android") version "2.57.1" apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
    id("com.android.test") version "8.12.3" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}