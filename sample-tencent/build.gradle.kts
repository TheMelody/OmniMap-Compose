import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

val formattedDate = SimpleDateFormat("yyyyMMddHHmm").format(Date())
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    load(FileInputStream(keystorePropertiesFile))
}
val jksDir = keystorePropertiesFile.absolutePath.substring(
    0, keystorePropertiesFile.absolutePath.length - "keystore.properties".length
)

android {
    namespace = "com.melody.tencentmap.myapplication"
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        targetSdk = libs.versions.target.sdk.version.get().toInt()
        versionCode = libs.versions.lib.maven.version.code.get().toInt()
        versionName = libs.versions.lib.maven.version.name.get()

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(jksDir + keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
        create("release") {
            storeFile = file(jksDir + keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "tencent_${defaultConfig.versionName}_${formattedDate}-${buildType.name}.apk"
        }
    }
}

dependencies {
    implementation(project(":sample-common"))
    implementation(project(":sample-ui-components"))
    implementation(project(":tencent-map-compose"))
    // 定位SDK
    implementation(libs.tencent.location.map)
    debugImplementation(libs.leakcanary.android)
}
