package com.segunfrancis.newsfeed

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.segunfrancis.newsfeed.worker.NewsFeedScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NewsFeedApplication : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var syncScheduler: NewsFeedScheduler

    override fun onCreate() {
        super.onCreate()

        syncScheduler.schedule()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Handler(Looper.getMainLooper()).post {
                StrictMode.setThreadPolicy(
                    ThreadPolicy.Builder().detectAll().penaltyLog().build()
                )
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
                )
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(true)
            .logger(DebugLogger())
            .placeholder(R.drawable.image_placeholder)
            .build()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()
}
