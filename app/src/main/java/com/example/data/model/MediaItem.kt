package com.example.data.model

data class MediaItem(
    val id: String,
    val title: String,
    val poster: String,
    val watchUrl: String,
    val genres: List<String>,
    val rank: Int?,
    val banner: String,
    val description: String,
    val quality: String,
    val type: String, // "Movie" or "Series" or "Anime Movie"
    val imdb: Double?,
    val cast: List<String>,
    val duration: String,
    val director: String,
    val releaseDate: String,
    val releaseYear: String,
    val rating: String,
    val language: String,
    val trailerUrl: String,
    val logoUrl: String
) {
    val isMovie: Boolean
        get() = type.contains("Movie", ignoreCase = true) || !type.contains("Series", ignoreCase = true)

    val isSeries: Boolean
        get() = type.contains("Series", ignoreCase = true)

    val displayType: String
        get() = if (type.contains("Series", ignoreCase = true)) "Series" else "Movie"

    val displayPoster: String
        get() = poster.ifBlank { banner }

    val displayBanner: String
        get() = banner.ifBlank { poster }

    val hasTrailer: Boolean
        get() = trailerUrl.isNotBlank() && (trailerUrl.startsWith("http://") || trailerUrl.startsWith("https://"))

    val hasWatch: Boolean
        get() = watchUrl.isNotBlank() && (watchUrl.startsWith("http://") || watchUrl.startsWith("https://"))
}

data class UserProfile(
    val id: String,
    val name: String,
    val avatarId: Int = 0
)
