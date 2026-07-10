package com.segunfrancis.newsfeed.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.segunfrancis.newsfeed.ui.components.menuItems
import javax.inject.Inject

class NewsFeedScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun schedule() {
        workManager.enqueueUniquePeriodicWork(
            NewsFeedWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            NewsFeedWorker.buildRequest(
                categories = menuItems.map { it.queryParam },
            )
        )
    }
}
