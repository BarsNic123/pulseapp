package com.example.pulse

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Ensures Firebase is initialized before any activity uses Auth or Firestore.
 * Replace app/google-services.json whenever you change the Firebase Android app or package name.
 */
class PulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
