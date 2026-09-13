import dev.icerock.gradle.MRVisibility
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.moko.resources)
}

kotlin {
    android {
        namespace = "de.chennemann.plannr.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        androidResources.enable = true
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PlannrApp"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }

        commonMain.dependencies {
//            implementation(projects.bridge)
            implementation(projects.client.database)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.compose.grid)

            implementation(libs.navigation.compose)

            // DI
            implementation(libs.koin.compose)

            // KotlinX
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)

            // MOKO
            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")

        optIn.add("kotlin.time.ExperimentalTime")
    }
}

multiplatformResources {
    resourcesClassName.set("Res")
    resourcesPackage.set("de.chennemann.plannr.resources")
    resourcesVisibility.set(MRVisibility.Internal)
    resourcesSourceSets {
        getByName("commonMain").srcDirs("src/commonMain/composeResources")
    }
}
