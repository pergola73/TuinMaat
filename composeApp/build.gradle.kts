import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import androidx.room.gradle.RoomExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("androidx.room")
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tuinmaat.resources"
}

// room configuratie
configure<RoomExtension> {
    schemaDirectory("$projectDir/schemas")
}

val props = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { props.load(it) }
}
val geminiApiKey: String = props.getProperty("GEMINI_API_KEY") ?: (project.findProperty("GEMINI_API_KEY") as String? ?: "")
val plantnetApiKey: String = props.getProperty("PLANTNET_API_KEY") ?: (project.findProperty("PLANTNET_API_KEY") as String? ?: "")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Workaround for KSP / Kotlin Native issue on CI
            freeCompilerArgs += listOf("-Xdisable-phases=VerifyBitcode")
            linkerOpts("-framework", "Foundation", "-lsqlite3")
        }
        
        // Zorg dat ook unit tests op CI kunnen linken zonder de echte Firebase SDK
        iosTarget.binaries.all {
            linkerOpts("-Wl,-undefined,dynamic_lookup")
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle)
            implementation(libs.androidx.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            
            // Firebase KMP
            implementation(libs.firebase.kmp.auth)
            implementation(libs.firebase.kmp.firestore)
            implementation(libs.firebase.kmp.storage)

            // Ktor & Serialization
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(compose.materialIconsExtended)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            
            // Navigation
            implementation(libs.navigation.multiplatform)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor.v3)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            
            // Firebase Android
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.vertexai)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.androidx.biometric)
            implementation(libs.play.services.auth)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    // KSP iOS is definitively disabled on CI due to unresolvable Kotlin Native bug
    if (System.getenv("GITHUB_ACTIONS") != "true") {
        add("kspIosX64", libs.androidx.room.compiler)
        add("kspIosArm64", libs.androidx.room.compiler)
        add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    }
}

// Nodig voor Room multiplatform
ksp {
    arg("room.generateKotlin", "true")
}

android {
    namespace = "com.rvodevelopment.tuinmaat.composeapp"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        minSdk = 24

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "PLANTNET_API_KEY", "\"$plantnetApiKey\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
