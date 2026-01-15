package com.example.dave

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics

class DaveApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()

        // Enable Crashlytics for release builds
        crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        // Set some general app information
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("app_version_code", BuildConfig.VERSION_CODE.toString())
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)

        // Log app start
        crashlytics.log("Dave Application started")
    }
}
