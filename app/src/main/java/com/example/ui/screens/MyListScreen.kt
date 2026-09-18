package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MediaCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OttTab
import com.example.ui.viewmodel.OttViewModel

@Composable
fun MyListScreen(
    viewModel: OttViewModel,
    modifier: Modifier = Modifier
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Watchlist", "Continue Watching", "Watch History")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Activity & Library",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                if (selectedTabIndex == 2 && watchHistory.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAllHistory() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = FlameOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = FlameOrange,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) FlameOrange else TextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        when (selectedTabIndex) {
            0 -> {
                // Watchlist / Bookmarks
                if (watchlist.isEmpty()) {
                    EmptyStateView(
                        title = "Your Watchlist is empty",
                        message = "Tap the bookmark icon on any anime or movie to save it here for later viewing.",
                        actionButtonText = "Discover Anime",
                        onActionClick = { viewModel.selectTab(OttTab.ALL_ANIME) }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${watchlist.size} saved titles", color = TextMuted, fontSize = 11.sp)
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 105.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchlist, key = { it.id }) { item ->
                            val mediaItem = libraryItems.find { it.id == item.id }
                            if (mediaItem != null) {
                                MediaCard(
                                    item = mediaItem,
                                    cardWidth = 110,
                                    cardHeight = 160,
                                    onClick = { viewModel.openDetails(mediaItem) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Continue Watching
                if (continueWatching.isEmpty()) {
                    EmptyStateView(
                        title = "Nothing in Continue Watching",
                        message = "When you start watching any title, your progress will be tracked right here.",
                        actionButtonText = "Start Watching Now",
                        onActionClick = { viewModel.selectTab(OttTab.HOME) }
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 105.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(continueWatching, key = { it.id }) { item ->
                            val mediaItem = libraryItems.find { it.id == item.id }
                            if (mediaItem != null) {
                                MediaCard(
                                    item = mediaItem,
                                    cardWidth = 110,
                                    cardHeight = 160,
                                    progressPercent = item.progressPercent,
                                    onRemoveProgress = { viewModel.removeContinueWatching(item.id) },
                                    onClick = { viewModel.openPlayer(mediaItem) }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Watch History
                if (watchHistory.isEmpty()) {
                    EmptyStateView(
                        title = "No Watch History",
                        message = "Titles you stream or watch will appear in your viewing history.",
                        actionButtonText = "Explore Home",
                        onActionClick = { viewModel.selectTab(OttTab.HOME) }
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 105.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchHistory, key = { it.id }) { item ->
                            val mediaItem = libraryItems.find { it.id == item.id }
                            if (mediaItem != null) {
                                MediaCard(
                                    item = mediaItem,
                                    cardWidth = 110,
                                    cardHeight = 160,
                                    onClick = { viewModel.openDetails(mediaItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
