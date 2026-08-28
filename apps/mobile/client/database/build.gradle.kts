@file:Suppress("unused")

import org.gradle.kotlin.dsl.support.delegates.NamedDomainObjectContainerDelegate
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        construct("PlannrDB") {
            packageName.set("de.chennemann.plannr.database")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
            deriveSchemaFromMigrations.set(true)
            generateAsync.set(false)
        }
    }
}

kotlin {
    android {
        namespace = "de.chennemann.plannr.database"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PlannrDB"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.bridge)
            implementation(libs.koin)

            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitives)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")

        optIn.add("kotlin.time.ExperimentalTime")
    }
}


// Helper function to help the compiler infer the type of the create function in the sqldelight configuration block
fun <T: Any> NamedDomainObjectContainerDelegate<T>.construct(name: String, configureAction: Action<in T>) = create(name) {
    configureAction.execute(this)
}
