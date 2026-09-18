package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.components.MediaCard
import com.example.ui.components.SectionTitle
import com.example.ui.components.SkeletonCard
import com.example.ui.components.Top10Card
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.StarGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OttTab
import com.example.ui.viewmodel.OttViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: OttViewModel,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val refreshMessage by viewModel.refreshMessage.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    val heroItems = viewModel.heroBannerItems
    var heroIndex by remember { mutableIntStateOf(0) }

    // Auto-advance hero banner every 6 seconds
    LaunchedEffect(heroItems.size) {
        if (heroItems.isNotEmpty()) {
            while (true) {
                delay(6000)
                heroIndex = (heroIndex + 1) % heroItems.size
            }
        }
    }

    val currentHero = heroItems.getOrNull(heroIndex)

    if (isLoading && heroItems.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AniStream",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = FlameOrange
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Loading your library from Google Sheets...",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    SkeletonCard()
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // --- 1. HERO BANNER CAROUSEL ---
        if (currentHero != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                ) {
                    AnimatedContent(
                        targetState = currentHero,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "hero_anim"
                    ) { hero ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = hero.displayBanner,
                                contentDescription = hero.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Multi-layer gradients for readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x99090C14),
                                                Color(0x22090C14),
                                                Color(0xCC090C14),
                                                DarkBackground
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Content overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Title Logo or Text
                        if (currentHero.logoUrl.isNotBlank() && currentHero.logoUrl.startsWith("http")) {
                            AsyncImage(
                                model = currentHero.logoUrl,
                                contentDescription = currentHero.title,
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth(0.65f),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.BottomStart
                            )
                        } else {
                            Text(
                                text = currentHero.title,
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metadata tags
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentHero.imdb != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "%.1f".format(currentHero.imdb),
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(FlameOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, FlameOrange, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentHero.quality,
                                    color = FlameOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = currentHero.releaseYear,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Text(
                                text = currentHero.genres.take(2).joinToString(" / "),
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Text(
                            text = currentHero.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Watch Now, Trailer, My List
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openPlayer(currentHero) },
                                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("hero_watch_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Watch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            if (currentHero.hasTrailer) {
                                OutlinedButton(
                                    onClick = { viewModel.openTrailer(currentHero) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(DarkBorder, CyberCyan))),
                                    modifier = Modifier.testTag("hero_trailer_button")
                                ) {
                                    Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Trailer", fontSize = 13.sp)
                                }
                            }

                            val inWatchlist = viewModel.isItemInWatchlist(currentHero.id)
                            IconButton(
                                onClick = { viewModel.toggleWatchlist(currentHero) },
                                modifier = Modifier
                                    .background(DarkSurfaceVariant, CircleShape)
                                    .size(40.dp)
                                    .testTag("hero_watchlist_toggle")
                            ) {
                                Icon(
                                    imageVector = if (inWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "My List",
                                    tint = if (inWatchlist) FlameOrange else TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Carousel Dots & Prev/Next navigation
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                heroItems.indices.forEach { idx ->
                                    Box(
                                        modifier = Modifier
                                            .height(4.dp)
                                            .width(if (idx == heroIndex) 20.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == heroIndex) FlameOrange else TextMuted.copy(alpha = 0.5f))
                                            .clickable { heroIndex = idx }
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        heroIndex = if (heroIndex > 0) heroIndex - 1 else heroItems.size - 1
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        heroIndex = (heroIndex + 1) % heroItems.size
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. CONTINUE WATCHING (if any) ---
        if (continueWatching.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Continue Watching",
                    subtitle = "Pick up where you left off"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(continueWatching) { item ->
                        val matchingMedia = viewModel.libraryItems.value.find { it.id == item.id }
                        if (matchingMedia != null) {
                            MediaCard(
                                item = matchingMedia,
                                progressPercent = item.progressPercent,
                                onRemoveProgress = { viewModel.removeContinueWatching(item.id) },
                                onClick = { viewModel.openPlayer(matchingMedia) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // --- 3. TRENDING NOW ---
        item {
            SectionTitle(
                title = "Trending Now",
                subtitle = "Most popular anime this week",
                onSeeAllClick = {
                    viewModel.selectTab(OttTab.ALL_ANIME)
                }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.trendingItems) { item ->
                    MediaCard(
                        item = item,
                        onClick = { viewModel.openDetails(item) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 4. TOP 10 MOVIES (Separated strictly!) ---
        item {
            SectionTitle(
                title = "Top 10 Movies",
                subtitle = "Ranked anime movies"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(viewModel.top10Movies) { index, item ->
                    Top10Card(
                        item = item,
                        rankIndex = index + 1,
                        onClick = { viewModel.openDetails(item) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 5. TOP 10 SERIES (Separated strictly!) ---
        item {
            SectionTitle(
                title = "Top 10 Series",
                subtitle = "Ranked episodic anime"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(viewModel.top10Series) { index, item ->
                    Top10Card(
                        item = item,
                        rankIndex = index + 1,
                        onClick = { viewModel.openDetails(item) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 6. RECOMMENDED FOR YOU ---
        item {
            SectionTitle(
                title = "Recommended For You",
                subtitle = "Based on your watch tastes"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.recommendedItems) { item ->
                    MediaCard(
                        item = item,
                        onClick = { viewModel.openDetails(item) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 7. LATEST RELEASES ---
        item {
            SectionTitle(
                title = "Latest Releases",
                subtitle = "Newest theatrical & streaming arrivals"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.latestReleases) { item ->
                    MediaCard(
                        item = item,
                        onClick = { viewModel.openDetails(item) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 8. POPULAR ACTION ANIME ---
        val actionItems = viewModel.getItemsForGenre("Action")
        if (actionItems.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Action & Adrenaline",
                    subtitle = "High-octane battles & quests",
                    onSeeAllClick = {
                        viewModel.setAllAnimeGenre("Action")
                        viewModel.selectTab(OttTab.ALL_ANIME)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(actionItems) { item ->
                        MediaCard(
                            item = item,
                            onClick = { viewModel.openDetails(item) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // --- 9. POPULAR FANTASY ANIME ---
        val fantasyItems = viewModel.getItemsForGenre("Fantasy")
        if (fantasyItems.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Fantasy Realms",
                    subtitle = "Magic, isekai & legends",
                    onSeeAllClick = {
                        viewModel.setAllAnimeGenre("Fantasy")
                        viewModel.selectTab(OttTab.ALL_ANIME)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fantasyItems) { item ->
                        MediaCard(
                            item = item,
                            onClick = { viewModel.openDetails(item) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // --- 10. DRAMA & ROMANCE ---
        val romanceItems = viewModel.getItemsForGenre("Romance").ifEmpty { viewModel.getItemsForGenre("Drama") }
        if (romanceItems.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Romance & Drama",
                    subtitle = "Heartfelt stories & emotions",
                    onSeeAllClick = {
                        viewModel.setAllAnimeGenre("Romance")
                        viewModel.selectTab(OttTab.ALL_ANIME)
                    }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(romanceItems) { item ->
                        MediaCard(
                            item = item,
                            onClick = { viewModel.openDetails(item) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // --- 11. BROWSE BY GENRE CHIPS ---
        item {
            SectionTitle(
                title = "Browse by Genre",
                subtitle = "Explore across all genres",
                onSeeAllClick = {
                    viewModel.selectTab(OttTab.ALL_ANIME)
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.allUniqueGenres.forEach { genre ->
                    Box(
                        modifier = Modifier
                            .background(DarkSurfaceVariant, RoundedCornerShape(20.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.setAllAnimeGenre(genre)
                                viewModel.selectTab(OttTab.ALL_ANIME)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = genre,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 12. REFRESH LIBRARY BAR ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Sheets Content Engine",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = refreshMessage ?: "Connected • ${viewModel.libraryItems.value.size} titles ready to stream",
                            color = if (refreshMessage?.contains("Unable") == true) FlameOrange else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.loadLibrary(forceRefresh = true) },
                        enabled = !isRefreshing,
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRefreshing) "Updating..." else "Refresh", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
