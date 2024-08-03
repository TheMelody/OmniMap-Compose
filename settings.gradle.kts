@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    // Uncomment and adjust the resolutionStrategy if needed
    // resolutionStrategy {
    //     eachPlugin {
    //         when (requested.id.toString()) {
    //             "com.android.tools.build" -> useModule("com.android.tools.build:gradle:7.3.0")
    //             "com.huawei.agconnect" -> useModule("com.huawei.agconnect:agcp:1.9.1.300")
    //         }
    //     }
    // }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

rootProject.name = "OmniMap-Compose"
include(":gd-map-compose")
include(":tencent-map-compose")
include(":baidu-map-compose")
// include(":huawei-map-compose")
include(":sample-gaode")
include(":sample-baidu")
include(":sample-tencent")
// include(":sample-huawei")
include(":sample-google")
include(":sample-ui-components")
include(":sample-common")
