package com.lasante.tvkiosk.media

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.lasante.tvkiosk.data.ScreenSaverVideo
import com.lasante.tvkiosk.data.ScreenSaverVideoPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Caché en disco de videos (screen saver + institucional) vía Media3 SimpleCache. */
@UnstableApi
object VideoCache {
    private const val TAG = "VideoCache"
    private const val CACHE_DIR = "video_cache"
    private const val MAX_CACHE_BYTES = 512L * 1024 * 1024
    private const val PREFETCH_GAP_MS = 3_000L

    private val prefetchMutex = Mutex()
    private val completedUrls = mutableSetOf<String>()

    @Volatile
    private var cache: SimpleCache? = null

    fun dataSourceFactory(context: Context): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(getOrCreateCache(context.applicationContext))
            .setUpstreamDataSourceFactory(
                DefaultDataSource.Factory(context.applicationContext),
            )
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    /** Misma normalización que ExoPlayer — la clave de SimpleCache debe coincidir en prefetch y play. */
    fun normalizeVideoUri(url: String): Uri {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return Uri.EMPTY

        val parsed = Uri.parse(trimmed)
        val scheme = parsed.scheme ?: return Uri.parse(trimmed.replace(" ", "%20"))
        val authority = parsed.encodedAuthority ?: parsed.host
            ?: return Uri.parse(trimmed.replace(" ", "%20"))

        return Uri.Builder()
            .scheme(scheme)
            .encodedAuthority(authority)
            .apply {
                parsed.pathSegments.forEach { segment ->
                    if (segment.isNotBlank()) appendPath(segment)
                }
            }
            .encodedQuery(parsed.encodedQuery)
            .encodedFragment(parsed.encodedFragment)
            .build()
    }

    suspend fun prefetchScreenSaverVideos(
        context: Context,
        screenSaverVideos: List<ScreenSaverVideo>,
    ) {
        val urls = ScreenSaverVideoPolicy.filterPlaylist(screenSaverVideos).map { it.url }
        prefetchUrls(context, urls, label = "screen-saver")
    }

    suspend fun prefetchIntroVideos(
        context: Context,
        screenSaverVideos: List<ScreenSaverVideo>,
        institutionalVideoUrl: String?,
    ) {
        val urls = buildList {
            ScreenSaverVideoPolicy.filterPlaylist(screenSaverVideos).forEach { add(it.url) }
            institutionalVideoUrl?.let { add(it) }
        }
        prefetchUrls(context, urls, label = "intro")
    }

    suspend fun prefetchUrls(
        context: Context,
        urls: Collection<String>,
        label: String = "video",
    ) {
        val appContext = context.applicationContext
        val uniqueUrls = urls.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (uniqueUrls.isEmpty()) return

        prefetchMutex.withLock {
            val pending = uniqueUrls.filter { url ->
                url !in completedUrls && !isFullyCached(appContext, url)
            }
            if (pending.isEmpty()) {
                Log.i(TAG, "Prefetch $label: todo en caché (${uniqueUrls.size} url(s))")
                uniqueUrls.forEach { synchronized(completedUrls) { completedUrls.add(it) } }
                return
            }

            Log.i(TAG, "Prefetching ${pending.size} $label video(s) sequentially")
            val factory = dataSourceFactory(appContext)
            withContext(Dispatchers.IO) {
                pending.forEachIndexed { index, url ->
                    if (index > 0) delay(PREFETCH_GAP_MS)
                    if (prefetchSingle(factory, url)) {
                        synchronized(completedUrls) { completedUrls.add(url) }
                    }
                }
            }
        }
    }

    fun isFullyCached(context: Context, url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val cache = getOrCreateCache(context.applicationContext)
            val dataSpec = buildDataSpec(url)
            val cacheKey = dataSpec.key ?: return false
            val cachedLength = cache.getCachedLength(cacheKey, 0, C.LENGTH_UNSET.toLong())
            cachedLength > 0L
        } catch (error: Exception) {
            Log.w(TAG, "isFullyCached check failed url=$url: ${error.message}")
            false
        }
    }

    private fun getOrCreateCache(context: Context): SimpleCache {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val cacheDir = File(context.cacheDir, CACHE_DIR)
            return SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
                StandaloneDatabaseProvider(context),
            ).also { cache = it }
        }
    }

    private fun buildDataSpec(url: String): DataSpec =
        DataSpec.Builder()
            .setUri(normalizeVideoUri(url))
            .setFlags(DataSpec.FLAG_ALLOW_GZIP)
            .build()

    private fun prefetchSingle(factory: CacheDataSource.Factory, url: String): Boolean {
        return try {
            val dataSpec = buildDataSpec(url)
            CacheWriter(
                factory.createDataSource(),
                dataSpec,
                null,
                null,
            ).cache()
            Log.i(TAG, "Cached video: ${dataSpec.uri}")
            true
        } catch (error: Exception) {
            Log.w(TAG, "Prefetch failed url=$url: ${error.message}")
            false
        }
    }
}
