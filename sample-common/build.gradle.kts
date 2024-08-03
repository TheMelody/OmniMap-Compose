plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.melody.sample.common"
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        lint.targetSdk = libs.versions.target.sdk.version.get().toInt()

        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    api(libs.androidx.runtime)
    api(libs.core.ktx)
    api(libs.lifecycle.runtime.ktx)
    api(libs.lifecycle.viewmodel.compose)
    api(libs.activity.compose)
    api(libs.accompanist.permissions)
    api(libs.startup.runtime)
}
