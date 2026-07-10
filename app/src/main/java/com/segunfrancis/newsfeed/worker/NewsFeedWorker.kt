package com.segunfrancis.newsfeed.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.segunfrancis.newsfeed.domain.NewsFeedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

@HiltWorker
class NewsFeedWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: NewsFeedRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val categories = inputData.getStringArray(KEY_CATEGORIES)?.toList()
            ?: return Result.failure(
                Data.Builder()
                    .putString(KEY_ERROR, "No categories provided")
                    .build()
            )
        return try {
            repository.prefetchAll(categories)
            Result.success()
        } catch (_: UnknownHostException) {
            Result.retry()
        } catch (_: SocketTimeoutException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure(
                Data.Builder()
                    .putString(KEY_ERROR, e.message)
                    .build()
            )
        }
    }

    companion object {
        const val WORK_NAME = "news_feed_sync_work"
        private const val KEY_CATEGORIES = "key_categories"
        private const val KEY_ERROR = "key_error"

        fun buildRequest(categories: List<String>): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            return PeriodicWorkRequestBuilder<NewsFeedWorker>(
                repeatInterval = 3,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .setInputData(
                    Data.Builder()
                        .putStringArray(KEY_CATEGORIES, categories.toTypedArray())
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
        }
    }
}
