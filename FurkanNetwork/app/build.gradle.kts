import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// İmza bilgileri koda yazılmaz. keystore.properties (git'e eklenmez) varsa
// yayın APK'sı o anahtarla imzalanır; yoksa yüklenebilir olması için hata
// ayıklama (debug) anahtarı kullanılır.
val imzaDosyasi = rootProject.file("keystore.properties")
val imzaOzellikleri = Properties().apply {
    if (imzaDosyasi.exists()) imzaDosyasi.inputStream().use { load(it) }
}

android {
    namespace = "com.furkan.network"
    // Varsayılan API 36. apk-uret.ps1, bilgisayarda yalnızca API 35 yüklüyse 35'i iletebilir.
    val derlemeSdk = project.findProperty("furkanCompileSdk")?.toString()?.toIntOrNull() ?: 36
    compileSdk = derlemeSdk
    // apk-uret.ps1 bilgisayardaki build-tools sürümünü buradan iletebilir.
    project.findProperty("furkanBuildTools")?.toString()?.takeIf { it.isNotBlank() }?.let {
        buildToolsVersion = it
    }

    defaultConfig {
        applicationId = "com.furkan.network"
        minSdk = 26
        targetSdk = minOf(36, derlemeSdk)
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (imzaDosyasi.exists()) {
            create("yayin") {
                storeFile = rootProject.file(imzaOzellikleri.getProperty("storeFile"))
                storePassword = imzaOzellikleri.getProperty("storePassword")
                keyAlias = imzaOzellikleri.getProperty("keyAlias")
                keyPassword = imzaOzellikleri.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (imzaDosyasi.exists()) {
                signingConfigs.getByName("yayin")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
