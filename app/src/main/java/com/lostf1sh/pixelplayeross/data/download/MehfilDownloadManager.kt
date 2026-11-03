package com.lostf1sh.pixelplayeross.data.download

import android.content.Context
import android.os.Environment
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

class MehfilDownloadManager(context: Context) {
    private val applicationContext = context.applicationContext
    private val mediaDirectory = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        ?: applicationContext.filesDir
    private val cacheDirectory = File(mediaDirectory, "media-cache")
    private val actionFile = File(mediaDirectory, "downloads.actions")
    private val databaseProvider = StandaloneDatabaseProvider(applicationContext)

    val cache: SimpleCache = SimpleCache(
        cacheDirectory,
        NoOpCacheEvictor(),
        databaseProvider
    )

    private val upstreamFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("mehfil-android")

    val cacheDataSourceFactory: CacheDataSource.Factory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    val manager: DownloadManager = DownloadManager(
        applicationContext,
        databaseProvider,
        actionFile,
        upstreamFactory,
        Executors.newFixedThreadPool(3)
    )

    fun release() {
        manager.release()
        cache.release()
    }
}
