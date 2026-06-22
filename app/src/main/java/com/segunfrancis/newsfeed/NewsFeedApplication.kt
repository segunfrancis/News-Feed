package com.segunfrancis.newsfeed

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NewsFeedApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            // Ignores Cache-Control: no-cache / no-store headers from the CDN.
            // Without this, Coil obeys the server's caching instructions even
            // when your policies say ENABLED — the server wins by default.
            .respectCacheHeaders(false)
            .crossfade(true)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
