package com.example.pulse

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

object AuthUi {
    fun logoutAndGoLogin(context: Context) {
        FirebaseAuth.getInstance().signOut()
        context.startActivity(
            Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        if (context is android.app.Activity) context.finish()
    }
}
