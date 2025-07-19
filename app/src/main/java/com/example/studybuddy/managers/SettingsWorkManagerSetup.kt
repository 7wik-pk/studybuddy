package com.example.studybuddy.managers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

//handles work tasks
fun setupSettingWorkManager(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<SettingsWorkManager>(
        15, TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "SettingsWorkManager",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )


}

//syncs the local settings with the database
fun syncNow(context: Context) {
    val oneTimeRequest = OneTimeWorkRequestBuilder<SettingsWorkManager>().build()
    WorkManager.getInstance(context).enqueue(oneTimeRequest)
}
