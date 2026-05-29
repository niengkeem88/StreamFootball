import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

fun gradleString(name: String, defaultValue: String): String {
    val localProperties = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { localProperties.load(it) }
    }
    val projectProp = project.findProperty(name) as? String
    if (!projectProp.isNullOrBlank()) return projectProp
    val localProp = localProperties.getProperty(name)
    if (!localProp.isNullOrBlank()) return localProp
    return defaultValue
}

fun gradleBoolean(name: String, defaultValue: Boolean): Boolean =
    gradleString(name, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue

val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobAppId = gradleString("ADMOB_ANDROID_APP_ID", admobTestAppId)
val manifestAdmobAppId = admobAppId.ifBlank { admobTestAppId }

android {
    namespace = "com.matchpulse.live"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.matchpulse.live"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        manifestPlaceholders["admobAppId"] = manifestAdmobAppId

        buildConfigField("boolean", "ENABLE_ADS", gradleBoolean("ENABLE_ADS", true).toString())
        buildConfigField("String", "ADMOB_ANDROID_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "ADMOB_ANDROID_BANNER_ID", "\"${gradleString("ADMOB_ANDROID_BANNER_ID", "")}\"")
        buildConfigField("String", "ADMOB_ANDROID_INTERSTITIAL_ID", "\"${gradleString("ADMOB_ANDROID_INTERSTITIAL_ID", "")}\"")
        buildConfigField("String", "ADMOB_ANDROID_NATIVE_ID", "\"${gradleString("ADMOB_ANDROID_NATIVE_ID", "")}\"")
        buildConfigField("String", "ADMOB_ANDROID_REWARDED_ID", "\"${gradleString("ADMOB_ANDROID_REWARDED_ID", "")}\"")
        buildConfigField("String", "ADMOB_TEST_DEVICE_IDS", "\"${gradleString("ADMOB_TEST_DEVICE_IDS", "")}\"")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.google.dagger:hilt-android:2.57.1")
    kapt("com.google.dagger:hilt-compiler:2.57.1")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("com.google.android.gms:play-services-ads:24.3.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
}
