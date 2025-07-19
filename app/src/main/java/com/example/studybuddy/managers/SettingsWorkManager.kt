package com.example.studybuddy.managers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
//manages user settings across the app
class SettingsWorkManager(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    //applies the settings across the app
    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
        val hasPending = prefs.getBoolean("hasPendingSettingsUpdate", false)
        if (!hasPending) {
            Log.d("SettingsWorker", "No changes to upload.")
            return Result.success()
        }

        val user = FirebaseAuth.getInstance().currentUser ?: return Result.failure()

        val settings = hashMapOf(
            "theme" to prefs.getString("theme", "System Default"),
            "fontSize" to prefs.getString("fontSize", "Medium"),
            "contrast" to prefs.getString("contrast", "Normal"),
            "useLocation" to prefs.getBoolean("useLocation", false)
        )

        return try {
            FirebaseFirestore.getInstance()
                .collection("userSettings")
                .document(user.uid)
                .set(settings)
                .await()

            prefs.edit().putBoolean("hasPendingSettingsUpdate", false).apply()
            Log.d("SettingsWorker", "Settings uploaded successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SettingsWorker", "Upload failed: ${e.message}")
            Result.retry()
        }
    }

}
