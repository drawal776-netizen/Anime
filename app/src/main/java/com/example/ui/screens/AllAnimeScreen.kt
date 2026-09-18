package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MediaCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OttViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun AllAnimeScreen(
    viewModel: OttViewModel,
    modifier: Modifier = Modifier
) {
    val selectedGenre by viewModel.allAnimeGenre.collectAsState()
    val selectedType by viewModel.allAnimeType.collectAsState()
    val selectedYear by viewModel.allAnimeYear.collectAsState()
    val searchQuery by viewModel.allAnimeSearch.collectAsState()
    val selectedSort by viewModel.allAnimeSort.collectAsState()
    val batchLimit by viewModel.allAnimeBatchLimit.collectAsState()

    val filteredList = viewModel.allAnimeFilteredList
    val displayedItems = remember(filteredList, batchLimit) {
        filteredList.take(batchLimit)
    }
    val hasMore = filteredList.size > displayedItems.size

    val allGenres = listOf("All") + viewModel.allUniqueGenres
    val allYears = listOf("All") + viewModel.allUniqueYears.take(15)
    val typeOptions = listOf("All", "Movies", "Series")

    var isSortMenuOpen by remember { mutableStateOf(false) }
    var isFilterDrawerOpen by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()

    // Trigger infinite scroll when near end
    val isNearBottom by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(isNearBottom) {
        if (isNearBottom && hasMore) {
            viewModel.loadMoreAllAnime()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- 1. TOP HEADER & SEARCH BAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "All Anime",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Explore & discover the full library",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Sort Dropdown Button
                Box {
                    Button(
                        onClick = { isSortMenuOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("all_anime_sort_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = FlameOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedSort.displayName,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    DropdownMenu(
                        expanded = isSortMenuOpen,
                        onDismissRequest = { isSortMenuOpen = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = option.displayName,
                                            color = if (selectedSort == option) FlameOrange else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (selectedSort == option) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = FlameOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setAllAnimeSort(option)
                                    isSortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar within All Anime
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setAllAnimeSearch(it) },
                placeholder = {
                    Text(
                        text = if (selectedGenre.isNullOrBlank() || selectedGenre == "All") {
                            "Search across all anime by title, cast, director..."
                        } else {
                            "Search within ${selectedGenre} anime..."
                        },
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = FlameOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setAllAnimeSearch("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = FlameOrange,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("all_anime_search_field")
            )
        }

        // --- 2. "CHOOSE YOUR GENRE" SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CHOOSE YOUR GENRE",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${allGenres.size - 1} genres detected",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Horizontally scrollable genre chips/cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allGenres.forEach { genre ->
                    val isSelected = (selectedGenre == genre) || (selectedGenre.isNullOrBlank() && genre == "All")
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) FlameOrange else DarkCardBg,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FlameOrange else DarkBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                viewModel.setAllAnimeGenre(genre)
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("genre_chip_$genre")
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- 3. SPECIAL FILTERS: CONTENT-TYPE & RELEASE YEAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Filter Pills: All / Movies / Series
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    typeOptions.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyberCyan else DarkBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setAllAnimeType(type) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("type_filter_$type")
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) CyberCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Year selector toggle
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allYears.take(6).forEach { yr ->
                        val isSelected = selectedYear == yr
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) FlameOrange.copy(alpha = 0.25f) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (isSelected) FlameOrange else DarkBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setAllAnimeYear(yr) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = yr,
                                color = if (isSelected) FlameOrange else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Result count summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${displayedItems.size} of ${filteredList.size} anime",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (selectedGenre != "All" || selectedType != "All" || selectedYear != "All" || searchQuery.isNotBlank()) {
                    Text(
                        text = "Reset filters",
                        color = FlameOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                viewModel.setAllAnimeGenre("All")
                                viewModel.setAllAnimeType("All")
                                viewModel.setAllAnimeYear("All")
                                viewModel.setAllAnimeSearch("")
                            }
                            .padding(4.dp)
                    )
                }
            }
        }

        // --- 4. CONTENT GRID WITH LOAD MORE / EMPTY STATE ---
        if (displayedItems.isEmpty()) {
            EmptyStateView(
                title = "No anime found in this genre",
                message = "We couldn't find any anime matching \"$selectedGenre\" with your current filters. Try changing or resetting the filters.",
                actionButtonText = "Browse All Anime",
                onActionClick = {
                    viewModel.setAllAnimeGenre("All")
                    viewModel.setAllAnimeType("All")
                    viewModel.setAllAnimeYear("All")
                    viewModel.setAllAnimeSearch("")
                }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 105.dp),
                state = gridState,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedItems, key = { it.id }) { item ->
                    MediaCard(
                        item = item,
                        cardWidth = 110,
                        cardHeight = 160,
                        onClick = { viewModel.openDetails(item) }
                    )
                }

                // Load More item button / indicator at the bottom of the grid
                if (hasMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { viewModel.loadMoreAllAnime() },
                                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .testTag("load_more_button")
                            ) {
                                Text(
                                    text = "Load More Anime (${filteredList.size - displayedItems.size} remaining)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Or scroll down to load automatically",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
