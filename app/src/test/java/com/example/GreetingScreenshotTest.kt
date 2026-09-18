package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.MediaItem
import com.example.ui.components.MediaCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun media_card_screenshot() {
        val dummyItem = MediaItem(
            id = "test_1",
            title = "Spirited Away",
            poster = "",
            watchUrl = "https://example.com/watch",
            genres = listOf("Animation", "Adventure", "Fantasy"),
            rank = 1,
            banner = "",
            description = "A young girl enters a world of spirits.",
            quality = "4K",
            type = "Movie",
            imdb = 8.6,
            cast = listOf("Rumi Hiiragi"),
            duration = "125 min",
            director = "Hayao Miyazaki",
            releaseDate = "2001-07-20",
            releaseYear = "2001",
            rating = "PG",
            language = "Japanese",
            trailerUrl = "",
            logoUrl = ""
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                MediaCard(
                    item = dummyItem,
                    onClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/media_card.png")
    }
}
