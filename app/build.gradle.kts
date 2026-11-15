import java.io.ByteArrayOutputStream
import javax.inject.Inject

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mypersonalchatapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mypersonalchatapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}

// Forwards each connected device's localhost:<port> to this computer, so the app reaches the local chat server.
abstract class AdbReverseTask : DefaultTask() {

    @get:Internal
    abstract val adbExecutable: RegularFileProperty

    @get:Input
    abstract val port: Property<Int>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun reverse() {
        val adb = adbExecutable.get().asFile.absolutePath
        val rule = "tcp:${port.get()}"
        try {
            val devicesOutput = ByteArrayOutputStream()
            execOperations.exec {
                commandLine(adb, "devices")
                standardOutput = devicesOutput
                isIgnoreExitValue = true
            }
            val serials = devicesOutput.toString().lines()
                .drop(1)
                .map { it.trim().split(Regex("\\s+")) }
                .filter { it.size >= 2 && it[1] == "device" }
                .map { it[0] }

            if (serials.isEmpty()) {
                logger.warn("adbReverse: no device connected, so port ${port.get()} was not forwarded.")
                return
            }
            for (serial in serials) {
                val result = execOperations.exec {
                    commandLine(adb, "-s", serial, "reverse", rule, rule)
                    standardOutput = ByteArrayOutputStream()
                    isIgnoreExitValue = true
                }
                if (result.exitValue == 0) {
                    logger.lifecycle("adbReverse: $serial localhost:${port.get()} -> this computer's port ${port.get()}")
                } else {
                    logger.warn("adbReverse: could not forward port ${port.get()} on $serial.")
                }
            }
        } catch (e: Exception) {
            logger.warn("adbReverse: skipped (${e.message}).")
        }
    }
}

val adbReverse = tasks.register<AdbReverseTask>("adbReverse") {
    description = "Runs 'adb reverse tcp:3000 tcp:3000' on every connected device."
    adbExecutable.set(androidComponents.sdkComponents.adb)
    port.set(3000)
}

tasks.configureEach {
    if (name == "assembleDebug" || name == "installDebug") {
        finalizedBy(adbReverse)
    }
}