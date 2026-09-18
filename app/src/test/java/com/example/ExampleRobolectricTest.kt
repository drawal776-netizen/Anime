package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.parser.CsvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name is AniStream`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AniStream", appName)
    }

    @Test
    fun `verify csv parser parses all fields and formats`() {
        val sampleCsv = """
title,poster,watch,genre,rank,banner,description,quality,type,imdb,cast,duration,director,release date,rating,language,trailer link,movie and series logo link
"Spirited Away","https://img/poster1.jpg","https://watch/1","Animation, Adventure, Family",1,"https://img/banner1.jpg","A young girl enters a world of spirits.","4K","Movie","8.6/10","Rumi Hiiragi, Miyu Irino","125 min","Hayao Miyazaki","2001-07-20","PG","Japanese","https://youtube.com/watch?v=123","https://img/logo1.png"
"Frieren: Beyond Journey's End","https://img/poster2.jpg","https://watch/2","Adventure, Drama, Fantasy",1,"https://img/banner2.jpg","After the demon king is defeated...","HD","Series","8.9/10","Atsumi Tanezaki","24 min","Keiichiro Saito","2023-09-29","TV-14","Japanese","https://youtube.com/watch?v=456",""
        """.trimIndent()

        val items = CsvParser.parse(sampleCsv)
        assertEquals(2, items.size)

        val first = items[0]
        assertEquals("Spirited Away", first.title)
        assertEquals("Movie", first.displayType)
        assertEquals(listOf("Animation", "Adventure", "Family"), first.genres)
        assertEquals(1, first.rank)
        assertEquals(8.6, first.imdb ?: 0.0, 0.01)
        assertEquals("Hayao Miyazaki", first.director)
        assertEquals("2001", first.releaseYear)
        assertTrue(first.hasTrailer)

        val second = items[1]
        assertEquals("Frieren: Beyond Journey's End", second.title)
        assertEquals("Series", second.displayType)
        assertTrue(second.isSeries)
        assertEquals(8.9, second.imdb ?: 0.0, 0.01)
        assertEquals("2023", second.releaseYear)
    }
}
