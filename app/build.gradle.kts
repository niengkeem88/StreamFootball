import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
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

val appEnv = gradleString("APP_ENV", "development")
val isProduction = appEnv == "production"
val apiBaseUrl = gradleString("API_BASE_URL", if (isProduction) "" else "https://matchpulse.invalid")
val footballApiMode = gradleString("FOOTBALL_API_MODE", "mock")
val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobAppId = gradleString("ADMOB_ANDROID_APP_ID", if (isProduction) "" else admobTestAppId)
val manifestAdmobAppId = admobAppId.ifBlank { if (isProduction) "" else admobTestAppId }
val enableAdsDefault = !isProduction

android {
    namespace = "com.matchpulse.live"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.matchpulse.live"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["admobAppId"] = manifestAdmobAppId

        buildConfigField("String", "APP_ENV", "\"$appEnv\"")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "FOOTBALL_API_MODE", "\"$footballApiMode\"")
        buildConfigField("boolean", "ENABLE_ADS", gradleBoolean("ENABLE_ADS", enableAdsDefault).toString())
        buildConfigField("String", "ADMOB_ANDROID_APP_ID", "\"$admobAppId\"")
        buildConfigField("String", "ADMOB_ANDROID_BANNER_ID", "\"${gradleString("ADMOB_ANDROID_BANNER_ID", "")}\"")
        buildConfigField("String", "ADMOB_ANDROID_INTERSTITIAL_ID", "\"${gradleString("ADMOB_ANDROID_INTERSTITIAL_ID", "")}\"")
        buildConfigField("String", "ADMOB_ANDROID_NATIVE_ID", "\"${gradleString("ADMOB_ANDROID_NATIVE_ID", "")}\"")
        buildConfigField("String", "ADMOB_ANDROID_REWARDED_ID", "\"${gradleString("ADMOB_ANDROID_REWARDED_ID", "")}\"")
        buildConfigField("String", "ADMOB_TEST_DEVICE_IDS", "\"${gradleString("ADMOB_TEST_DEVICE_IDS", "")}\"")
        buildConfigField("boolean", "ENABLE_TEAM_LOGOS", gradleBoolean("ENABLE_TEAM_LOGOS", true).toString())
        buildConfigField("boolean", "ENABLE_LEGAL_PROVIDER_LINKS", gradleBoolean("ENABLE_LEGAL_PROVIDER_LINKS", true).toString())
        buildConfigField("boolean", "ENABLE_EXPERIMENTAL_PLAYER", gradleBoolean("ENABLE_EXPERIMENTAL_PLAYER", false).toString())
        buildConfigField("String", "FOOTBALL_API_KEY", "\"${gradleString("FOOTBALL_API_KEY", "")}\"")
        buildConfigField("String", "FOOTBALL_API_HOST", "\"v3.football.api-sports.io\"")
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
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("com.google.android.gms:play-services-ads:24.3.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
