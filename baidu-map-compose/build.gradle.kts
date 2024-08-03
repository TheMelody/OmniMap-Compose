plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    alias(libs.plugins.compose.compiler)
}

afterEvaluate {
    apply(from = "../gradle/release/baidu_publish.gradle")
}

android {
    namespace = "com.melody.map.baidu_compose"
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        lint.targetSdk = libs.versions.target.sdk.version.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        jvmTarget = "19"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.lifecycle.runtime.ktx)

    // 地图组件
    api(libs.baidu.map)
}
