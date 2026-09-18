package com.example.data.parser

import com.example.data.model.MediaItem
import java.io.BufferedReader
import java.io.StringReader
import java.security.MessageDigest

object CsvParser {

    fun parse(csvText: String): List<MediaItem> {
        val rows = parseCsvRows(csvText)
        if (rows.isEmpty()) return emptyList()

        val headerRow = rows[0]
        val headerMap = mutableMapOf<String, Int>()
        headerRow.forEachIndexed { index, colName ->
            headerMap[colName.trim().lowercase()] = index
        }

        val items = mutableListOf<MediaItem>()
        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.isEmpty() || row.all { it.isBlank() }) continue

            fun getCol(headerName: String, fallbackIndex: Int): String {
                val index = headerMap[headerName.lowercase()] ?: fallbackIndex
                return if (index in row.indices) row[index].trim() else ""
            }

            val title = getCol("title", 0)
            if (title.isBlank()) continue

            val poster = getCol("poster", 1)
            val watch = getCol("watch", 2)
            val genreRaw = getCol("genre", 3)
            val rankRaw = getCol("rank", 4)
            val banner = getCol("banner", 5)
            val description = getCol("description", 6)
            val quality = getCol("quality", 7).ifBlank { "HD" }
            val type = getCol("type", 8).ifBlank { "Movie" }
            val imdbRaw = getCol("imdb", 9)
            val castRaw = getCol("cast", 10)
            val duration = getCol("duration", 11)
            val director = getCol("director", 12)
            val releaseDate = getCol("release date", 13)
            val rating = getCol("rating", 14).ifBlank { "TV-14" }
            val language = getCol("language", 15).ifBlank { "Japanese" }
            val trailer = getCol("trailer link", 16)
            val logo = getCol("movie and series logo link", 17)

            // Parse genres
            val genres = genreRaw.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals("N/A", ignoreCase = true) }
                .let { if (it.isEmpty()) listOf("Anime") else it }

            // Parse cast
            val castList = castRaw.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals("N/A", ignoreCase = true) }

            // Parse IMDb rating
            val imdb = imdbRaw.replace("/10", "").trim().toDoubleOrNull()

            // Parse rank
            val rank = rankRaw.filter { it.isDigit() }.toIntOrNull()

            // Parse release year
            val year = extractYear(releaseDate)

            val id = generateId(title, releaseDate, i)

            items.add(
                MediaItem(
                    id = id,
                    title = title,
                    poster = poster,
                    watchUrl = watch,
                    genres = genres,
                    rank = rank,
                    banner = banner,
                    description = description.ifBlank { "No description available." },
                    quality = quality,
                    type = type,
                    imdb = imdb,
                    cast = castList,
                    duration = duration.ifBlank { "N/A" },
                    director = if (director.isBlank() || director.equals("N/A", ignoreCase = true)) "Unknown" else director,
                    releaseDate = releaseDate,
                    releaseYear = year,
                    rating = rating,
                    language = language,
                    trailerUrl = trailer,
                    logoUrl = logo
                )
            )
        }
        return items
    }

    private fun extractYear(dateStr: String): String {
        val regex = Regex("""\b(19\d\d|20\d\d)\b""")
        val match = regex.find(dateStr)
        return match?.value ?: "2024"
    }

    private fun generateId(title: String, releaseDate: String, index: Int): String {
        val input = "$title-$releaseDate-$index"
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.joinToString("") { "%02x".format(it) }.take(12)
        } catch (e: Exception) {
            "item_$index"
        }
    }

    private fun parseCsvRows(csvText: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val reader = BufferedReader(StringReader(csvText))
        var currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false

        var charInt: Int
        while (reader.read().also { charInt = it } != -1) {
            val c = charInt.toChar()

            if (inQuotes) {
                if (c == '"') {
                    reader.mark(1)
                    val nextInt = reader.read()
                    if (nextInt != -1 && nextInt.toChar() == '"') {
                        currentField.append('"')
                    } else {
                        reader.reset()
                        inQuotes = false
                    }
                } else {
                    currentField.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                    }
                    '\r' -> {
                        // ignore carriage return
                    }
                    '\n' -> {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                        result.add(currentRow)
                        currentRow = mutableListOf()
                    }
                    else -> currentField.append(c)
                }
            }
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            result.add(currentRow)
        }

        return result
    }
}
