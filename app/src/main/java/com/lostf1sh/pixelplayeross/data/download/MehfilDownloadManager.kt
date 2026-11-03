package com.lostf1sh.pixelplayeross.data.download

import android.content.Context
import android.os.Environment
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import okhttp3.OkHttpClient
import java.io.File

class MehfilDownloadManager(context: Context, okHttpClient: OkHttpClient) {
    private val applicationContext = context.applicationContext
    private val cacheDirectory = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "media-cache")

    val cache: SimpleCache = SimpleCache(
        cacheDirectory,
        NoOpCacheEvictor(),
        StandaloneDatabaseProvider(applicationContext)
    )

    private val upstreamFactory = OkHttpDataSource.Factory(okHttpClient)
        .setUserAgent("mehfil-android")

    val cacheDataSourceFactory: CacheDataSource.Factory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    val manager: DownloadManager = DownloadManager(
        applicationContext,
        StandaloneDatabaseProvider(applicationContext),
        cacheDirectory.resolve("downloads"),
        upstreamFactory,
        java.util.concurrent.Executors.newFixedThreadPool(3)
    )

    fun release() {
        manager.release()
        cache.release()
    }
}
