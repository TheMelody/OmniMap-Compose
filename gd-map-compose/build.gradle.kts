plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

afterEvaluate {
    apply(from = "../gradle/release/gaode_publish.gradle")
}

android {
    namespace = "com.melody.map.gd_compose"
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        lint.targetSdk =  libs.versions.target.sdk.version.get().toInt()
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
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.lifecycle.runtime.ktx)

    // 高德地图3d
    api(libs.gaode.map3d)
    // 高德地图：搜索相关API，路径规划需要用到这里面的类
    api(libs.gaode.search)
}
