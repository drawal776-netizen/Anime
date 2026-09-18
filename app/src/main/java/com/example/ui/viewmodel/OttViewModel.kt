package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ContinueWatchingEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistItemEntity
import com.example.data.model.MediaItem
import com.example.data.repository.SheetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OttTab {
    HOME,
    BROWSE,
    ALL_ANIME,
    SEARCH,
    MY_LIST,
    PROFILE
}

enum class SortOption(val displayName: String) {
    LATEST("Latest Added"),
    RELEASE_DATE("Release Date"),
    A_Z("A – Z"),
    Z_A("Z – A"),
    HIGHEST_RATING("Highest Rating"),
    HIGHEST_IMDB("Highest IMDb"),
    RANK("Top Rank")
}

class OttViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SheetRepository(application, database.mediaDao())

    private val _libraryItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val libraryItems: StateFlow<List<MediaItem>> = _libraryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshMessage = MutableStateFlow<String?>(null)
    val refreshMessage: StateFlow<String?> = _refreshMessage.asStateFlow()

    private val _selectedTab = MutableStateFlow(OttTab.HOME)
    val selectedTab: StateFlow<OttTab> = _selectedTab.asStateFlow()

    private val _activeDetailsItem = MutableStateFlow<MediaItem?>(null)
    val activeDetailsItem: StateFlow<MediaItem?> = _activeDetailsItem.asStateFlow()

    private val _activePlayerItem = MutableStateFlow<MediaItem?>(null)
    val activePlayerItem: StateFlow<MediaItem?> = _activePlayerItem.asStateFlow()

    private val _activeTrailerItem = MutableStateFlow<MediaItem?>(null)
    val activeTrailerItem: StateFlow<MediaItem?> = _activeTrailerItem.asStateFlow()

    // --- All Anime Screen Specific State ---
    private val _allAnimeGenre = MutableStateFlow<String?>("All")
    val allAnimeGenre: StateFlow<String?> = _allAnimeGenre.asStateFlow()

    private val _allAnimeType = MutableStateFlow("All")
    val allAnimeType: StateFlow<String> = _allAnimeType.asStateFlow()

    private val _allAnimeYear = MutableStateFlow("All")
    val allAnimeYear: StateFlow<String> = _allAnimeYear.asStateFlow()

    private val _allAnimeSearch = MutableStateFlow("")
    val allAnimeSearch: StateFlow<String> = _allAnimeSearch.asStateFlow()

    private val _allAnimeSort = MutableStateFlow(SortOption.LATEST)
    val allAnimeSort: StateFlow<SortOption> = _allAnimeSort.asStateFlow()

    private val _allAnimeBatchLimit = MutableStateFlow(24)
    val allAnimeBatchLimit: StateFlow<Int> = _allAnimeBatchLimit.asStateFlow()

    // --- Browse Screen Specific State ---
    private val _browseType = MutableStateFlow("All")
    val browseType: StateFlow<String> = _browseType.asStateFlow()

    private val _browseGenre = MutableStateFlow("All")
    val browseGenre: StateFlow<String> = _browseGenre.asStateFlow()

    private val _browseLanguage = MutableStateFlow("All")
    val browseLanguage: StateFlow<String> = _browseLanguage.asStateFlow()

    private val _browseSort = MutableStateFlow(SortOption.LATEST)
    val browseSort: StateFlow<SortOption> = _browseSort.asStateFlow()

    // --- Global Search State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(listOf("Naruto", "Attack on Titan", "Demon Slayer", "Fantasy", "Action"))
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _searchSelectedType = MutableStateFlow("All")
    val searchSelectedType: StateFlow<String> = _searchSelectedType.asStateFlow()

    // --- Room Observables ---
    val watchlist: StateFlow<List<WatchlistItemEntity>> = repository.watchlistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<ContinueWatchingEntity>> = repository.continueWatchingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profiles: StateFlow<List<UserProfileEntity>> = repository.profilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProfile = MutableStateFlow(UserProfileEntity("p1", "Anime Otaku", 0))
    val activeProfile: StateFlow<UserProfileEntity> = _activeProfile.asStateFlow()

    init {
        loadLibrary(forceRefresh = false)
        initDefaultProfiles()
    }

    private fun initDefaultProfiles() {
        viewModelScope.launch {
            repository.createProfile("Anime Otaku", 0)
            repository.createProfile("Movie Night", 1)
            repository.createProfile("Guest", 2)
        }
    }

    fun loadLibrary(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
                _refreshMessage.value = "Updating library..."
            } else {
                _isLoading.value = true
            }

            val result = repository.fetchLibrary(forceRefresh)
            result.onSuccess { items ->
                _libraryItems.value = items
                if (forceRefresh) {
                    _refreshMessage.value = "Library updated (${items.size} titles)"
                }
            }.onFailure { error ->
                if (forceRefresh) {
                    _refreshMessage.value = "Unable to load the library. Try refreshing."
                }
            }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun dismissRefreshMessage() {
        _refreshMessage.value = null
    }

    fun selectTab(tab: OttTab) {
        _selectedTab.value = tab
    }

    fun openDetails(item: MediaItem) {
        _activeDetailsItem.value = item
    }

    fun closeDetails() {
        _activeDetailsItem.value = null
    }

    fun openPlayer(item: MediaItem) {
        _activePlayerItem.value = item
        viewModelScope.launch {
            repository.updateContinueWatching(item, 0.25f)
        }
    }

    fun closePlayer() {
        _activePlayerItem.value = null
    }

    fun openTrailer(item: MediaItem) {
        if (item.hasTrailer) {
            _activeTrailerItem.value = item
        }
    }

    fun closeTrailer() {
        _activeTrailerItem.value = null
    }

    // Watchlist
    fun toggleWatchlist(item: MediaItem) {
        viewModelScope.launch {
            val exists = watchlist.value.any { it.id == item.id }
            if (exists) {
                repository.removeFromWatchlist(item.id)
            } else {
                repository.addToWatchlist(item)
            }
        }
    }

    fun isItemInWatchlist(id: String): Boolean {
        return watchlist.value.any { it.id == id }
    }

    // Continue Watching
    fun removeContinueWatching(id: String) {
        viewModelScope.launch {
            repository.removeContinueWatching(id)
        }
    }

    // History
    fun removeFromHistory(id: String) {
        viewModelScope.launch {
            repository.removeFromHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Profile
    fun switchProfile(profile: UserProfileEntity) {
        _activeProfile.value = profile
    }

    // Dynamic Lists from Google Sheet data
    val allUniqueGenres: List<String>
        get() = _libraryItems.value
            .flatMap { it.genres }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    val allUniqueLanguages: List<String>
        get() = _libraryItems.value
            .map { it.language.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    val allUniqueYears: List<String>
        get() = _libraryItems.value
            .map { it.releaseYear }
            .filter { it.length == 4 && it.all { ch -> ch.isDigit() } }
            .distinct()
            .sortedDescending()

    // --- All Anime Screen Controls ---
    fun setAllAnimeGenre(genre: String?) {
        _allAnimeGenre.value = genre
        _allAnimeBatchLimit.value = 24
    }

    fun setAllAnimeType(type: String) {
        _allAnimeType.value = type
        _allAnimeBatchLimit.value = 24
    }

    fun setAllAnimeYear(year: String) {
        _allAnimeYear.value = year
        _allAnimeBatchLimit.value = 24
    }

    fun setAllAnimeSearch(query: String) {
        _allAnimeSearch.value = query
        _allAnimeBatchLimit.value = 24
    }

    fun setAllAnimeSort(sort: SortOption) {
        _allAnimeSort.value = sort
    }

    fun loadMoreAllAnime() {
        _allAnimeBatchLimit.value += 24
    }

    // All Anime Screen Filtered List
    val allAnimeFilteredList: List<MediaItem>
        get() {
            val genre = _allAnimeGenre.value
            val type = _allAnimeType.value
            val year = _allAnimeYear.value
            val search = _allAnimeSearch.value.trim().lowercase()
            val sort = _allAnimeSort.value

            return _libraryItems.value.asSequence()
                .filter { item ->
                    if (genre.isNullOrBlank() || genre.equals("All", ignoreCase = true)) {
                        true
                    } else {
                        item.genres.any { it.equals(genre, ignoreCase = true) }
                    }
                }
                .filter { item ->
                    when (type) {
                        "Movies" -> item.isMovie
                        "Series" -> item.isSeries
                        else -> true
                    }
                }
                .filter { item ->
                    if (year == "All") true else item.releaseYear == year
                }
                .filter { item ->
                    if (search.isBlank()) true else {
                        item.title.lowercase().contains(search) ||
                                item.genres.any { it.lowercase().contains(search) } ||
                                item.director.lowercase().contains(search) ||
                                item.cast.any { it.lowercase().contains(search) }
                    }
                }
                .let { seq ->
                    when (sort) {
                        SortOption.LATEST -> seq.sortedByDescending { it.releaseDate }
                        SortOption.RELEASE_DATE -> seq.sortedByDescending { it.releaseDate }
                        SortOption.A_Z -> seq.sortedBy { it.title.lowercase() }
                        SortOption.Z_A -> seq.sortedByDescending { it.title.lowercase() }
                        SortOption.HIGHEST_RATING -> seq.sortedByDescending { it.imdb ?: 0.0 }
                        SortOption.HIGHEST_IMDB -> seq.sortedByDescending { it.imdb ?: 0.0 }
                        SortOption.RANK -> seq.sortedWith(compareBy<MediaItem> { it.rank ?: 9999 }.thenByDescending { it.imdb ?: 0.0 })
                    }
                }
                .toList()
        }

    // --- Browse Screen Controls ---
    fun setBrowseType(type: String) { _browseType.value = type }
    fun setBrowseGenre(genre: String) { _browseGenre.value = genre }
    fun setBrowseLanguage(lang: String) { _browseLanguage.value = lang }
    fun setBrowseSort(sort: SortOption) { _browseSort.value = sort }

    val browseFilteredList: List<MediaItem>
        get() {
            val type = _browseType.value
            val genre = _browseGenre.value
            val lang = _browseLanguage.value
            val sort = _browseSort.value

            return _libraryItems.value.asSequence()
                .filter { item ->
                    when (type) {
                        "Movies" -> item.isMovie
                        "Series" -> item.isSeries
                        else -> true
                    }
                }
                .filter { item ->
                    if (genre == "All") true else item.genres.any { it.equals(genre, ignoreCase = true) }
                }
                .filter { item ->
                    if (lang == "All") true else item.language.equals(lang, ignoreCase = true)
                }
                .let { seq ->
                    when (sort) {
                        SortOption.LATEST -> seq.sortedByDescending { it.releaseDate }
                        SortOption.RELEASE_DATE -> seq.sortedByDescending { it.releaseDate }
                        SortOption.A_Z -> seq.sortedBy { it.title.lowercase() }
                        SortOption.Z_A -> seq.sortedByDescending { it.title.lowercase() }
                        SortOption.HIGHEST_RATING -> seq.sortedByDescending { it.imdb ?: 0.0 }
                        SortOption.HIGHEST_IMDB -> seq.sortedByDescending { it.imdb ?: 0.0 }
                        SortOption.RANK -> seq.sortedWith(compareBy<MediaItem> { it.rank ?: 9999 }.thenByDescending { it.imdb ?: 0.0 })
                    }
                }
                .toList()
        }

    // --- Global Search Controls ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun executeSearch(query: String) {
        _searchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.isNotBlank() && !_searchHistory.value.contains(trimmed)) {
            _searchHistory.value = listOf(trimmed) + _searchHistory.value.take(7)
        }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun removeSearchHistoryItem(item: String) {
        _searchHistory.value = _searchHistory.value.filter { it != item }
    }

    fun setSearchSelectedType(type: String) {
        _searchSelectedType.value = type
    }

    val globalSearchResults: List<MediaItem>
        get() {
            val q = _searchQuery.value.trim().lowercase()
            if (q.isBlank()) return emptyList()

            val typeFilter = _searchSelectedType.value

            return _libraryItems.value.asSequence()
                .filter { item ->
                    when (typeFilter) {
                        "Movies" -> item.isMovie
                        "Series" -> item.isSeries
                        else -> true
                    }
                }
                .filter { item ->
                    item.title.lowercase().contains(q) ||
                            item.genres.any { it.lowercase().contains(q) } ||
                            item.cast.any { it.lowercase().contains(q) } ||
                            item.director.lowercase().contains(q) ||
                            item.language.lowercase().contains(q) ||
                            item.description.lowercase().contains(q) ||
                            item.type.lowercase().contains(q)
                }
                .sortedByDescending { it.imdb ?: 0.0 }
                .toList()
        }

    // --- Home Screen Shelves ---
    val heroBannerItems: List<MediaItem>
        get() = _libraryItems.value
            .filter { it.banner.isNotBlank() && (it.imdb ?: 0.0) >= 7.5 }
            .take(6)
            .ifEmpty { _libraryItems.value.take(6) }

    val trendingItems: List<MediaItem>
        get() = _libraryItems.value
            .sortedByDescending { (it.imdb ?: 0.0) }
            .take(15)

    val top10Movies: List<MediaItem>
        get() {
            val movies = _libraryItems.value.filter { it.isMovie }
            // Sort by rank if present, else by IMDb
            val ranked = movies.filter { it.rank != null }.sortedBy { it.rank }
            val unranked = movies.filter { it.rank == null }.sortedByDescending { it.imdb ?: 0.0 }
            return (ranked + unranked).take(10)
        }

    val top10Series: List<MediaItem>
        get() {
            val series = _libraryItems.value.filter { it.isSeries }
            val ranked = series.filter { it.rank != null }.sortedBy { it.rank }
            val unranked = series.filter { it.rank == null }.sortedByDescending { it.imdb ?: 0.0 }
            return (ranked + unranked).take(10)
        }

    val latestReleases: List<MediaItem>
        get() = _libraryItems.value
            .sortedByDescending { it.releaseDate }
            .take(15)

    val recentlyAdded: List<MediaItem>
        get() = _libraryItems.value.takeLast(15).reversed()

    val popularMovies: List<MediaItem>
        get() = _libraryItems.value.filter { it.isMovie }.sortedByDescending { it.imdb ?: 0.0 }.take(15)

    val popularSeries: List<MediaItem>
        get() = _libraryItems.value.filter { it.isSeries }.sortedByDescending { it.imdb ?: 0.0 }.take(15)

    val recommendedItems: List<MediaItem>
        get() {
            // Pick genres from watchlist / continue watching if available
            val watchedIds = continueWatching.value.map { it.id }.toSet()
            val watchlistIds = watchlist.value.map { it.id }.toSet()
            val favoriteGenres = _libraryItems.value
                .filter { it.id in watchedIds || it.id in watchlistIds }
                .flatMap { it.genres }
                .toSet()

            return if (favoriteGenres.isNotEmpty()) {
                _libraryItems.value
                    .filter { item -> item.id !in watchedIds && item.genres.any { it in favoriteGenres } }
                    .sortedByDescending { it.imdb ?: 0.0 }
                    .take(15)
                    .ifEmpty { _libraryItems.value.shuffled().take(15) }
            } else {
                _libraryItems.value.filter { (it.imdb ?: 0.0) >= 7.8 }.take(15)
            }
        }

    fun getItemsForGenre(genre: String, limit: Int = 12): List<MediaItem> {
        return _libraryItems.value
            .filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }
            .sortedByDescending { it.imdb ?: 0.0 }
            .take(limit)
    }

    fun getSimilarItems(item: MediaItem, limit: Int = 10): List<MediaItem> {
        val targetGenres = item.genres.toSet()
        return _libraryItems.value
            .filter { it.id != item.id && it.genres.any { g -> g in targetGenres } }
            .sortedByDescending { it.imdb ?: 0.0 }
            .take(limit)
    }
}
