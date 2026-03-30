plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

fun propertyOrEnv(name: String): String? = providers.gradleProperty(name)
    .orElse(providers.environmentVariable(name))
    .orNull
    ?.takeIf { it.isNotBlank() }

fun propertyOrEnv(name: String, defaultValue: String): String = propertyOrEnv(name) ?: defaultValue

fun intPropertyOrEnv(name: String, defaultValue: Int): Int =
    propertyOrEnv(name)?.toIntOrNull() ?: defaultValue

val releaseVersionCode = intPropertyOrEnv("SPLITWISE_VERSION_CODE", 1)
val releaseVersionName = propertyOrEnv("SPLITWISE_VERSION_NAME", "1.0")
val apiBaseUrl = propertyOrEnv("SPLITWISE_API_BASE_URL", "https://api.splitwise.ir/api/v1")
val bazaarStoreUrl = propertyOrEnv(
    "SPLITWISE_BAZAAR_STORE_URL",
    "https://cafebazaar.ir/app/com.encer.splitwise"
)
val myketStoreUrl = propertyOrEnv(
    "SPLITWISE_MYKET_STORE_URL",
    "https://myket.ir/app/com.encer.splitwise"
)
val organicStoreUrl = propertyOrEnv(
    "SPLITWISE_ORGANIC_STORE_URL",
    "http://splitwise.ir/download-app"
)

val releaseStoreFile = propertyOrEnv("SPLITWISE_RELEASE_STORE_FILE")
val releaseStorePassword = propertyOrEnv("SPLITWISE_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = propertyOrEnv("SPLITWISE_RELEASE_KEY_ALIAS")
val releaseKeyPassword = propertyOrEnv("SPLITWISE_RELEASE_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.encer.splitwise"
    compileSdk {
        version = release(36)
    }
    flavorDimensions += "distribution"

    defaultConfig {
        applicationId = "com.encer.splitwise"
        minSdk = 24
        targetSdk = 36
        versionCode = releaseVersionCode
        versionName = releaseVersionName
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    productFlavors {
        create("bazaar") {
            dimension = "distribution"
            buildConfigField("String", "STORE_CHANNEL", "\"bazaar\"")
            buildConfigField("String", "DEFAULT_STORE_URL", "\"$bazaarStoreUrl\"")
        }
        create("myket") {
            dimension = "distribution"
            buildConfigField("String", "STORE_CHANNEL", "\"myket\"")
            buildConfigField("String", "DEFAULT_STORE_URL", "\"$myketStoreUrl\"")
        }
        create("organic") {
            dimension = "distribution"
            buildConfigField("String", "STORE_CHANNEL", "\"organic\"")
            buildConfigField("String", "DEFAULT_STORE_URL", "\"$organicStoreUrl\"")
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }
    androidResources {
        localeFilters += listOf("fa")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
        }
    }
    testOptions {
        managedDevices {
            localDevices {
                create("phoneApi35") {
                    device = "Pixel 6"
                    apiLevel = 35
                    systemImageSource = "aosp-atd"
                }
                create("tabletApi35") {
                    device = "Pixel Tablet"
                    apiLevel = 35
                    systemImageSource = "aosp-atd"
                }
            }
            groups {
                create("responsiveSmoke") {
                    targetDevices.add(allDevices["phoneApi35"])
                    targetDevices.add(allDevices["tabletApi35"])
                }
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

configurations.configureEach {
    resolutionStrategy.force(
        "androidx.fragment:fragment:1.8.5",
        "androidx.lifecycle:lifecycle-livedata-core-ktx:2.9.1"
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
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
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.hilt.android)
    debugImplementation(libs.chucker)
    add("releaseImplementation", libs.chucker.noop)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
