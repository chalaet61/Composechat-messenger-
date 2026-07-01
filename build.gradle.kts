// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

tasks.register<Copy>("copyApkToDownload") {
    from(file("${project.rootDir}/.build-outputs/app-debug.apk"))
    into(file("${project.rootDir}/APK_DOWNLOAD"))
}

val rootDirFile = project.rootDir

tasks.register("verifyApkDetails") {
    val apkFile = File(rootDirFile, "APK_DOWNLOAD/app-debug.apk")
    doLast {
        if (apkFile.exists()) {
            val sizeInBytes = apkFile.length()
            val sizeInMb = sizeInBytes.toDouble() / (1024 * 1024)
            println("APK File exists at: ${apkFile.absolutePath}")
            println("APK File size: $sizeInBytes bytes ($sizeInMb MB)")
            if (sizeInBytes > 1024 * 1024) {
                println("APK verified successfully: Size is greater than 1 MB.")
            } else {
                throw GradleException("APK size is less than 1 MB!")
            }
        } else {
            throw GradleException("APK file does not exist!")
        }
    }
}



