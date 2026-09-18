package com.example.data.repository

import android.content.Context
import com.example.data.local.ContinueWatchingEntity
import com.example.data.local.MediaDao
import com.example.data.local.UserProfileEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistItemEntity
import com.example.data.model.MediaItem
import com.example.data.parser.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class SheetRepository(
    private val context: Context,
    private val mediaDao: MediaDao
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val csvUrl = "https://docs.google.com/spreadsheets/d/1i3thy-hRD_WFsJmfNDDSKg1PFQ3gluSBVBRGJ2TTryc/export?format=csv"
    private val cacheFile = File(context.cacheDir, "sheet_cache.csv")

    suspend fun fetchLibrary(forceRefresh: Boolean = false): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            // Check cache if not force refresh
            if (!forceRefresh && cacheFile.exists() && cacheFile.length() > 100) {
                val cachedText = cacheFile.readText()
                val items = CsvParser.parse(cachedText)
                if (items.isNotEmpty()) {
                    return@withContext Result.success(items)
                }
            }

            // Fetch live CSV
            val request = Request.Builder()
                .url(csvUrl)
                .header("User-Agent", "Mozilla/5.0 (AniStream OTT Engine)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Fallback to cache if available
                    if (cacheFile.exists() && cacheFile.length() > 100) {
                        val cachedText = cacheFile.readText()
                        val items = CsvParser.parse(cachedText)
                        return@withContext Result.success(items)
                    }
                    return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                }

                val bodyText = response.body?.string() ?: ""
                if (bodyText.isNotBlank()) {
                    cacheFile.writeText(bodyText)
                    val items = CsvParser.parse(bodyText)
                    return@withContext Result.success(items)
                } else {
                    return@withContext Result.failure(Exception("Empty response body from sheet"))
                }
            }
        } catch (e: Exception) {
            // Fallback to cache on error
            if (cacheFile.exists() && cacheFile.length() > 100) {
                try {
                    val cachedText = cacheFile.readText()
                    val items = CsvParser.parse(cachedText)
                    if (items.isNotEmpty()) {
                        return@withContext Result.success(items)
                    }
                } catch (ignored: Exception) {}
            }
            Result.failure(e)
        }
    }

    // Watchlist
    val watchlistFlow: Flow<List<WatchlistItemEntity>> = mediaDao.getWatchlist()

    suspend fun addToWatchlist(item: MediaItem) = withContext(Dispatchers.IO) {
        mediaDao.addToWatchlist(
            WatchlistItemEntity(
                id = item.id,
                title = item.title,
                poster = item.displayPoster,
                type = item.displayType,
                rating = item.rating,
                imdb = item.imdb
            )
        )
    }

    suspend fun removeFromWatchlist(id: String) = withContext(Dispatchers.IO) {
        mediaDao.removeFromWatchlist(id)
    }

    fun isInWatchlist(id: String): Flow<Boolean> = mediaDao.isInWatchlist(id)

    // Continue Watching
    val continueWatchingFlow: Flow<List<ContinueWatchingEntity>> = mediaDao.getContinueWatching()

    suspend fun updateContinueWatching(item: MediaItem, progressPercent: Float = 0.35f) = withContext(Dispatchers.IO) {
        mediaDao.upsertContinueWatching(
            ContinueWatchingEntity(
                id = item.id,
                title = item.title,
                poster = item.displayPoster,
                type = item.displayType,
                duration = item.duration,
                progressPercent = progressPercent,
                lastWatchedTimestamp = System.currentTimeMillis()
            )
        )
        // Also record in history
        mediaDao.addToHistory(
            WatchHistoryEntity(
                id = item.id,
                title = item.title,
                poster = item.displayPoster,
                type = item.displayType,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeContinueWatching(id: String) = withContext(Dispatchers.IO) {
        mediaDao.removeContinueWatching(id)
    }

    // History
    val watchHistoryFlow: Flow<List<WatchHistoryEntity>> = mediaDao.getWatchHistory()

    suspend fun removeFromHistory(id: String) = withContext(Dispatchers.IO) {
        mediaDao.removeFromHistory(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        mediaDao.clearHistory()
    }

    // Profiles
    val profilesFlow: Flow<List<UserProfileEntity>> = mediaDao.getProfiles()

    suspend fun createProfile(name: String, avatarIndex: Int) = withContext(Dispatchers.IO) {
        mediaDao.insertProfile(
            UserProfileEntity(
                id = "prof_${System.currentTimeMillis()}",
                name = name,
                avatarIndex = avatarIndex
            )
        )
    }
}
